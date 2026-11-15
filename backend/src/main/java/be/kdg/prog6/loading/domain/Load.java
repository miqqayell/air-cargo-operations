package be.kdg.prog6.loading.domain;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.events.DomainEvent;
import be.kdg.prog6.common.events.LoadFinalisedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Load {

    private final FlightId flightId;
    private final AirportCode destination;
    private final LocalDateTime scheduledDeparture;
    private LocalDateTime estimatedDeparture;
    private final List<Uld> ulds = new ArrayList<>();
    private LoadStatus status = LoadStatus.OPEN;
    private boolean departed;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Load(FlightId flightId, AirportCode destination, LocalDateTime scheduledDeparture,
                 LocalDateTime estimatedDeparture) {
        this.flightId = flightId;
        this.destination = destination;
        this.scheduledDeparture = scheduledDeparture;
        this.estimatedDeparture = estimatedDeparture;
    }

    public static Load openFor(FlightId flightId, AirportCode destination, LocalDateTime scheduledDeparture) {
        return new Load(flightId, destination, scheduledDeparture, scheduledDeparture);
    }

    public static Load restore(FlightId flightId, AirportCode destination, LocalDateTime scheduledDeparture,
                               LocalDateTime estimatedDeparture, LoadStatus status, boolean departed,
                               List<Uld> ulds) {
        Load load = new Load(flightId, destination, scheduledDeparture, estimatedDeparture);
        load.status = status;
        load.departed = departed;
        load.ulds.addAll(ulds);
        return load;
    }

    public void estimateDeparture(LocalDateTime estimatedDeparture) {
        this.estimatedDeparture = estimatedDeparture;
    }

    public LoadReport check(List<LoadCheck> checks, FloorPlan floorPlan) {
        return new LoadReport(checks.stream().map(check -> check.check(this, floorPlan)).toList());
    }

    public LoadingWindow loadingWindow(LoadingPolicy policy) {
        return new LoadingWindow(scheduledDeparture.minus(policy.windowOpensBeforeScheduledDeparture()),
                estimatedDeparture.minus(policy.windowClosesBeforeEstimatedDeparture()));
    }

    public void markDeparted() {
        departed = true;
    }

    public boolean bookUld(UldCode code, Weight weight, AirportCode uldDestination) {
        if (departed) {
            throw new IllegalStateException("Flight " + flightId + " has departed, cargo cannot be booked on it");
        }
        if (findUld(code) != null) {
            return false;
        }
        ulds.add(new Uld(code, weight, uldDestination));
        return true;
    }

    public void assignPosition(UldCode code, Position position, FloorPlan floorPlan, LoadingPolicy policy,
                               LocalDateTime now) {
        requireChangeable(policy, now);
        if (!floorPlan.contains(position)) {
            throw new IllegalArgumentException(
                    "Position " + position.code() + " does not exist on a " + floorPlan.aircraftType());
        }
        Uld uld = bookedUld(code);
        Uld occupant = findUldAt(position);
        if (occupant != null && occupant != uld) {
            throw new PositionOccupiedException(position);
        }
        uld.placeAt(position);
    }

    public void takeOffDeck(UldCode code, LoadingPolicy policy, LocalDateTime now) {
        requireChangeable(policy, now);
        bookedUld(code).takeOffDeck();
    }

    private void requireChangeable(LoadingPolicy policy, LocalDateTime now) {
        if (status == LoadStatus.FINAL) {
            throw new IllegalStateException("The load is final and can no longer change");
        }
        loadingWindow(policy).requireOpenAt(now);
    }

    private Uld bookedUld(UldCode code) {
        Uld uld = findUld(code);
        if (uld == null) {
            throw new IllegalArgumentException("ULD " + code.value() + " is not booked on this flight");
        }
        return uld;
    }

    public void declareFinal(FloorPlan floorPlan, LoadingPolicy policy, boolean softWarningsConfirmed,
                             LocalDateTime now) {
        requireChangeable(policy, now);
        LoadReport report = check(policy.loadChecks(), floorPlan);
        if (!report.hardChecksPass()) {
            throw new LoadChecksFailedException("The load cannot be declared final while a hard check fails",
                    report.failedHardChecks());
        }
        if (!report.softWarnings().isEmpty() && !softWarningsConfirmed) {
            throw new LoadChecksFailedException("Confirm the soft warnings before declaring the load final",
                    report.softWarnings());
        }
        status = LoadStatus.FINAL;
        List<LoadFinalisedEvent.PlacedUld> placedUlds = ulds.stream()
                .filter(Uld::isPlaced)
                .map(uld -> new LoadFinalisedEvent.PlacedUld(uld.getCode().value(), uld.getPosition().code(),
                        uld.getWeight().value(), uld.getWeight().unit().name(), uld.getDestination().code()))
                .toList();
        domainEvents.add(new LoadFinalisedEvent(UUID.randomUUID(), flightId, placedUlds));
    }

    private Uld findUld(UldCode code) {
        return ulds.stream()
                .filter(uld -> uld.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    private Uld findUldAt(Position position) {
        return ulds.stream()
                .filter(uld -> position.equals(uld.getPosition()))
                .findFirst()
                .orElse(null);
    }

    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    public FlightId getFlightId() {
        return flightId;
    }

    public AirportCode getDestination() {
        return destination;
    }

    public LocalDateTime getScheduledDeparture() {
        return scheduledDeparture;
    }

    public LocalDateTime getEstimatedDeparture() {
        return estimatedDeparture;
    }

    public LoadStatus getStatus() {
        return status;
    }

    public boolean isDeparted() {
        return departed;
    }

    public List<Uld> getUlds() {
        return List.copyOf(ulds);
    }
}
