package be.kdg.prog6.flights.domain;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.common.events.DepartureRecordedEvent;
import be.kdg.prog6.common.events.EstimatedDepartureChangedEvent;
import be.kdg.prog6.common.events.FlightScheduledEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FlightTest {

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

    @Test
    void schedulingAFlightRecordsAnEvent() {
        assertEquals(1, flight.getDomainEvents().size());
        assertInstanceOf(FlightScheduledEvent.class, flight.getDomainEvents().getFirst());
    }

    @Test
    void withoutDelaysEstimatedTimesEqualScheduledTimes() {
        assertEquals(STD, flight.getEstimatedDeparture());
        assertEquals(STA, flight.getEstimatedArrival());
    }

    @Test
    void departureDelayShiftsDepartureAndArrival() {
        flight.registerDelay(DelaySide.DEPARTURE, 30, "93", "Late inbound aircraft");

        assertEquals(STD.plusMinutes(30), flight.getEstimatedDeparture());
        assertEquals(STA.plusMinutes(30), flight.getEstimatedArrival());
    }

    @Test
    void arrivalDelayShiftsOnlyArrival() {
        flight.registerDelay(DelaySide.ARRIVAL, 20, "87", "Airport congestion");

        assertEquals(STD, flight.getEstimatedDeparture());
        assertEquals(STA.plusMinutes(20), flight.getEstimatedArrival());
    }

    @Test
    void delayLongerThanTwelveHoursIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> flight.registerDelay(DelaySide.DEPARTURE, 721, "93", "Too long"));
    }

    @Test
    void flightCannotDepartBeforeItsLoadIsFinal() {
        assertThrows(InvalidFlightStateException.class, () -> flight.recordDeparture(STD, NOW, TOLERANCE));
    }

    @Test
    void departedFlightRefusesDepartureDelay() {
        flight.receiveFinalLoad();
        flight.recordDeparture(STD, NOW, TOLERANCE);

        assertThrows(InvalidFlightStateException.class,
                () -> flight.registerDelay(DelaySide.DEPARTURE, 10, "93", "Too late"));
    }

    @Test
    void flightMustDepartBeforeItCanLand() {
        assertThrows(InvalidFlightStateException.class, () -> flight.recordArrival(STA, NOW, TOLERANCE));
    }

    @Test
    void recordingDepartureAndArrivalChangesTheStatus() {
        flight.receiveFinalLoad();

        flight.recordDeparture(STD, NOW, TOLERANCE);
        assertEquals(FlightStatus.DEPARTED, flight.getStatus());

        flight.recordArrival(STA, NOW, TOLERANCE);
        assertEquals(FlightStatus.LANDED, flight.getStatus());
    }

    @Test
    void departureDelayAnnouncesNewEstimatedDepartureAndArrival() {
        flight.clearDomainEvents();

        flight.registerDelay(DelaySide.DEPARTURE, 30, "93", "Late inbound aircraft");

        assertEquals(2, flight.getDomainEvents().size());
        var departure = assertInstanceOf(EstimatedDepartureChangedEvent.class, flight.getDomainEvents().get(0));
        var arrival = assertInstanceOf(EstimatedArrivalChangedEvent.class, flight.getDomainEvents().get(1));
        assertEquals(STD.plusMinutes(30), departure.estimatedDeparture());
        assertEquals(STA.plusMinutes(30), arrival.estimatedArrival());
    }

    @Test
    void arrivalDelayOnlyAnnouncesNewEstimatedArrival() {
        flight.clearDomainEvents();

        flight.registerDelay(DelaySide.ARRIVAL, 20, "87", "Airport congestion");

        assertEquals(1, flight.getDomainEvents().size());
        var arrival = assertInstanceOf(EstimatedArrivalChangedEvent.class, flight.getDomainEvents().getFirst());
        assertEquals(STA.plusMinutes(20), arrival.estimatedArrival());
    }

    @Test
    void recordingActualTimesAnnouncesThem() {
        flight.receiveFinalLoad();
        flight.clearDomainEvents();

        flight.recordDeparture(STD.plusMinutes(3), NOW, TOLERANCE);
        flight.recordArrival(STA.plusMinutes(1), NOW, TOLERANCE);

        var departure = assertInstanceOf(DepartureRecordedEvent.class, flight.getDomainEvents().get(0));
        var arrival = assertInstanceOf(ArrivalRecordedEvent.class, flight.getDomainEvents().get(1));
        assertEquals(STD.plusMinutes(3), departure.actualDeparture());
        assertEquals(STA.plusMinutes(1), arrival.actualArrival());
    }
}
