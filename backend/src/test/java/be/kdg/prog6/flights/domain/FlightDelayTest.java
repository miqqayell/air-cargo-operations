package be.kdg.prog6.flights.domain;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.common.events.EstimatedDepartureChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightDelayTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 2, 0, 0);
    private static final Duration TOLERANCE = Duration.ofMinutes(5);

    private static final LocalDateTime STD = LocalDateTime.of(2026, 10, 1, 6, 30);
    private static final LocalDateTime STA = LocalDateTime.of(2026, 10, 1, 10, 0);

    private Flight flight;

    @BeforeEach
    void setUp() {
        flight = Flight.schedule(new FlightNumber("HJ221"), new AirportCode("BRU"), new AirportCode("ATH"),
                new AircraftRegistration("SXBAA"), STD, STA);
    }

    private DelayId register(DelaySide side, int minutes) {
        flight.registerDelay(side, minutes, "93", "Late inbound aircraft");
        flight.clearDomainEvents();
        return flight.getDelays().getLast().getId();
    }

    private List<Class<?>> recordedEvents() {
        return flight.getDomainEvents().stream().<Class<?>>map(Object::getClass).toList();
    }

    @Test
    void correctingADepartureDelayMovesBothEstimatesAndAnnouncesThem() {
        DelayId delay = register(DelaySide.DEPARTURE, 30);

        flight.changeDelay(delay, 45, "Late inbound aircraft from ATH");

        assertEquals(STD.plusMinutes(45), flight.getEstimatedDeparture());
        assertEquals(STA.plusMinutes(45), flight.getEstimatedArrival());
        assertEquals("Late inbound aircraft from ATH", flight.getDelays().getFirst().getDescription());
        assertEquals(List.of(EstimatedDepartureChangedEvent.class, EstimatedArrivalChangedEvent.class), recordedEvents());
    }

    @Test
    void correctingAnArrivalDelayOnlyAnnouncesTheArrival() {
        DelayId delay = register(DelaySide.ARRIVAL, 20);

        flight.changeDelay(delay, 10, "Congestion");

        assertEquals(STA.plusMinutes(10), flight.getEstimatedArrival());
        assertEquals(List.of(EstimatedArrivalChangedEvent.class), recordedEvents());
    }

    @Test
    void correctingOnlyTheDescriptionAnnouncesNothing() {
        DelayId delay = register(DelaySide.DEPARTURE, 30);

        flight.changeDelay(delay, 30, "Another description");

        assertTrue(flight.getDomainEvents().isEmpty());
    }

    @Test
    void removingTheLastDelayBringsTheEstimatesBackToTheSchedule() {
        DelayId delay = register(DelaySide.DEPARTURE, 30);

        flight.removeDelay(delay);

        assertEquals(STD, flight.getEstimatedDeparture());
        assertEquals(STA, flight.getEstimatedArrival());
        assertTrue(flight.getDelays().isEmpty());
        var departure = assertInstanceOf(EstimatedDepartureChangedEvent.class, flight.getDomainEvents().get(0));
        assertEquals(STD, departure.estimatedDeparture());
    }

    @Test
    void afterDepartureOnlyArrivalDelaysCanChange() {
        DelayId departureDelay = register(DelaySide.DEPARTURE, 30);
        DelayId arrivalDelay = register(DelaySide.ARRIVAL, 10);
        flight.receiveFinalLoad();
        flight.recordDeparture(STD.plusMinutes(30), NOW, TOLERANCE);

        assertThrows(InvalidFlightStateException.class, () -> flight.changeDelay(departureDelay, 40, "Later"));
        assertThrows(InvalidFlightStateException.class, () -> flight.removeDelay(departureDelay));
        flight.changeDelay(arrivalDelay, 15, "Congestion");
        assertEquals(STA.plusMinutes(45), flight.getEstimatedArrival());
    }

    @Test
    void afterLandingNoDelayCanChange() {
        DelayId arrivalDelay = register(DelaySide.ARRIVAL, 10);
        flight.receiveFinalLoad();
        flight.recordDeparture(STD, NOW, TOLERANCE);
        flight.recordArrival(STA.plusMinutes(10), NOW, TOLERANCE);

        assertThrows(InvalidFlightStateException.class, () -> flight.changeDelay(arrivalDelay, 15, "Later"));
        assertThrows(InvalidFlightStateException.class, () -> flight.removeDelay(arrivalDelay));
    }

    @Test
    void correctedDelayStillRespectsTheLimits() {
        DelayId delay = register(DelaySide.DEPARTURE, 30);

        assertThrows(IllegalArgumentException.class, () -> flight.changeDelay(delay, 0, "None"));
        assertThrows(IllegalArgumentException.class, () -> flight.changeDelay(delay, 30, " "));
        assertEquals(30, flight.getDelays().getFirst().getMinutes());
    }

    @Test
    void delayCodeMustBeOnTheIataList() {
        assertThrows(IllegalArgumentException.class,
                () -> flight.registerDelay(DelaySide.DEPARTURE, 10, "00X", "Unknown reason"));
        assertTrue(flight.getDelays().isEmpty());
    }

    @Test
    void unknownDelayIsNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> flight.removeDelay(new DelayId(UUID.randomUUID())));
    }
}
