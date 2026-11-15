package be.kdg.prog6.flights.domain;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.common.events.DepartureRecordedEvent;
import be.kdg.prog6.common.events.DomainEvent;
import be.kdg.prog6.common.events.EstimatedDepartureChangedEvent;
import be.kdg.prog6.common.events.FlightRemovedEvent;
import be.kdg.prog6.common.events.FlightScheduledEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class Flight {

    private final FlightId id;
    private final AirportCode arrivalAirport;
    private final AircraftRegistration aircraftRegistration;
    private final LocalDateTime scheduledDeparture;
    private final LocalDateTime scheduledArrival;
    private final List<Delay> delays = new ArrayList<>();
    private LocalDateTime actualDeparture;
    private LocalDateTime actualArrival;
    private boolean finalLoadReceived;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Flight(FlightId id, AirportCode arrivalAirport, AircraftRegistration aircraftRegistration,
                   LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival) {
        this.id = id;
        this.arrivalAirport = arrivalAirport;
        this.aircraftRegistration = aircraftRegistration;
        this.scheduledDeparture = scheduledDeparture;
        this.scheduledArrival = scheduledArrival;
    }

    public static Flight schedule(FlightNumber flightNumber, AirportCode departureAirport, AirportCode arrivalAirport,
                                  AircraftRegistration aircraftRegistration,
                                  LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival) {
        if (!scheduledArrival.isAfter(scheduledDeparture)) {
            throw new IllegalArgumentException("The scheduled arrival must be after the scheduled departure");
        }
        FlightId id = new FlightId(flightNumber, scheduledDeparture.toLocalDate(), departureAirport);
        Flight flight = new Flight(id, arrivalAirport, aircraftRegistration, scheduledDeparture, scheduledArrival);
        flight.domainEvents.add(new FlightScheduledEvent(UUID.randomUUID(), id, arrivalAirport,
                scheduledDeparture, scheduledArrival));
        return flight;
    }


    public static Flight restore(FlightId id, AirportCode arrivalAirport, AircraftRegistration aircraftRegistration,
                                 LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival, List<Delay> delays,
                                 LocalDateTime actualDeparture, LocalDateTime actualArrival,
                                 boolean finalLoadReceived) {
        Flight flight = new Flight(id, arrivalAirport, aircraftRegistration, scheduledDeparture, scheduledArrival);
        flight.delays.addAll(delays);
        flight.actualDeparture = actualDeparture;
        flight.actualArrival = actualArrival;
        flight.finalLoadReceived = finalLoadReceived;
        return flight;
    }

    public FlightStatus getStatus() {
        if (actualArrival != null) {
            return FlightStatus.LANDED;
        }
        if (actualDeparture != null) {
            return FlightStatus.DEPARTED;
        }
        return FlightStatus.SCHEDULED;
    }

    public LocalDateTime getEstimatedDeparture() {
        return scheduledDeparture.plusMinutes(totalMinutes(DelaySide.DEPARTURE));
    }

    public LocalDateTime getEstimatedArrival() {
        return scheduledArrival.plusMinutes(totalMinutes(DelaySide.DEPARTURE) + totalMinutes(DelaySide.ARRIVAL));
    }

    private int totalMinutes(DelaySide side) {
        return delays.stream()
                .filter(delay -> delay.getSide() == side)
                .mapToInt(Delay::getMinutes)
                .sum();
    }

    public void registerDelay(DelaySide side, int minutes, String delayCode, String description) {
        requireDelaysCanChange(side);
        LocalDateTime departureBefore = getEstimatedDeparture();
        LocalDateTime arrivalBefore = getEstimatedArrival();
        delays.add(new Delay(side, minutes, delayCode, description));
        announceChangedEstimates(departureBefore, arrivalBefore);
    }

    public void changeDelay(DelayId delayId, int minutes, String description) {
        Delay delay = findDelay(delayId);
        requireDelaysCanChange(delay.getSide());
        LocalDateTime departureBefore = getEstimatedDeparture();
        LocalDateTime arrivalBefore = getEstimatedArrival();
        delays.set(delays.indexOf(delay), delay.correctedTo(minutes, description));
        announceChangedEstimates(departureBefore, arrivalBefore);
    }

    public void removeDelay(DelayId delayId) {
        Delay delay = findDelay(delayId);
        requireDelaysCanChange(delay.getSide());
        LocalDateTime departureBefore = getEstimatedDeparture();
        LocalDateTime arrivalBefore = getEstimatedArrival();
        delays.remove(delay);
        announceChangedEstimates(departureBefore, arrivalBefore);
    }

    private Delay findDelay(DelayId delayId) {
        return delays.stream()
                .filter(delay -> delay.getId().equals(delayId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No delay " + delayId.id() + " on flight " + id));
    }

    private void requireDelaysCanChange(DelaySide side) {
        if (getStatus() == FlightStatus.LANDED) {
            throw new InvalidFlightStateException("The flight has landed, its delays can no longer change");
        }
        if (getStatus() == FlightStatus.DEPARTED && side == DelaySide.DEPARTURE) {
            throw new InvalidFlightStateException("The flight has departed, its departure delays can no longer change");
        }
    }

    private void announceChangedEstimates(LocalDateTime departureBefore, LocalDateTime arrivalBefore) {
        if (!getEstimatedDeparture().equals(departureBefore)) {
            domainEvents.add(new EstimatedDepartureChangedEvent(UUID.randomUUID(), id, aircraftRegistration.value(),
                    getEstimatedDeparture()));
        }
        if (!getEstimatedArrival().equals(arrivalBefore)) {
            domainEvents.add(new EstimatedArrivalChangedEvent(UUID.randomUUID(), id, aircraftRegistration,
                    arrivalAirport, getEstimatedArrival()));
        }
    }

    public void receiveFinalLoad() {
        finalLoadReceived = true;
    }

    public void recordDeparture(LocalDateTime actualDeparture, LocalDateTime now, Duration tolerance) {
        if (getStatus() != FlightStatus.SCHEDULED) {
            throw new InvalidFlightStateException("The flight has already departed");
        }
        if (!finalLoadReceived) {
            throw new InvalidFlightStateException("The flight cannot depart before its load is final");
        }
        requireNotInTheFuture(actualDeparture, now);
        requireWithinTolerance("departure", actualDeparture, getEstimatedDeparture(), tolerance);
        this.actualDeparture = actualDeparture;
        domainEvents.add(new DepartureRecordedEvent(UUID.randomUUID(), id, aircraftRegistration.value(), actualDeparture));
    }

    public void recordArrival(LocalDateTime actualArrival, LocalDateTime now, Duration tolerance) {
        if (getStatus() != FlightStatus.DEPARTED) {
            throw new InvalidFlightStateException("Only a departed flight can land");
        }
        if (!actualArrival.isAfter(actualDeparture)) {
            throw new IllegalArgumentException("The actual arrival must be after the actual departure");
        }
        requireNotInTheFuture(actualArrival, now);
        requireWithinTolerance("arrival", actualArrival, getEstimatedArrival(), tolerance);
        this.actualArrival = actualArrival;
        domainEvents.add(new ArrivalRecordedEvent(UUID.randomUUID(), id, aircraftRegistration, arrivalAirport,
                actualArrival));
    }

    private static void requireNotInTheFuture(LocalDateTime actualTime, LocalDateTime now) {
        if (actualTime.isAfter(now)) {
            throw new IllegalArgumentException("An actual time cannot lie in the future");
        }
    }

    private static void requireWithinTolerance(String what, LocalDateTime actualTime, LocalDateTime estimate,
                                               Duration tolerance) {
        if (actualTime.isAfter(estimate.plus(tolerance))) {
            throw new InvalidFlightStateException("The actual " + what + " is more than " + tolerance.toMinutes()
                    + " minutes later than the estimated " + what + " (" + estimate.toLocalTime()
                    + " UTC): register a delay first");
        }
    }

    public void remove() {
        domainEvents.add(new FlightRemovedEvent(UUID.randomUUID(), id));
    }

    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    public FlightId getId() {
        return id;
    }

    public AirportCode getArrivalAirport() {
        return arrivalAirport;
    }

    public AircraftRegistration getAircraftRegistration() {
        return aircraftRegistration;
    }

    public LocalDateTime getScheduledDeparture() {
        return scheduledDeparture;
    }

    public LocalDateTime getScheduledArrival() {
        return scheduledArrival;
    }

    public LocalDateTime getActualDeparture() {
        return actualDeparture;
    }

    public LocalDateTime getActualArrival() {
        return actualArrival;
    }

    public List<Delay> getDelays() {
        return List.copyOf(delays);
    }

    public boolean isFinalLoadReceived() {
        return finalLoadReceived;
    }
}
