package be.kdg.prog6.flights.domain;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FlightActualTimeTest {

    private static final LocalDateTime STD = LocalDateTime.of(2026, 10, 1, 6, 30);
    private static final LocalDateTime STA = LocalDateTime.of(2026, 10, 1, 10, 0);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 1, 12, 0);
    private static final Duration TOLERANCE = Duration.ofMinutes(5);

    private Flight flight;

    @BeforeEach
    void setUp() {
        flight = Flight.schedule(new FlightNumber("HJ221"), new AirportCode("BRU"), new AirportCode("ATH"),
                new AircraftRegistration("SXBAA"), STD, STA);
        flight.receiveFinalLoad();
    }

    @Test
    void departureExactlyAtTheToleranceIsAllowed() {
        flight.recordDeparture(STD.plusMinutes(5), NOW, TOLERANCE);

        assertEquals(STD.plusMinutes(5), flight.getActualDeparture());
    }

    @Test
    void departureLaterThanTheToleranceRequiresADelayFirst() {
        var refusal = assertThrows(InvalidFlightStateException.class,
                () -> flight.recordDeparture(STD.plusMinutes(6), NOW, TOLERANCE));

        assertEquals("The actual departure is more than 5 minutes later than the estimated departure (06:30 UTC): "
                + "register a delay first", refusal.getMessage());
    }

    @Test
    void afterADelayTheToleranceCountsFromTheEstimate() {
        flight.registerDelay(DelaySide.DEPARTURE, 30, "93", "Late inbound aircraft");

        flight.recordDeparture(STD.plusMinutes(35), NOW, TOLERANCE);

        assertEquals(FlightStatus.DEPARTED, flight.getStatus());
    }

    @Test
    void earlierThanPlannedIsAlwaysAllowed() {
        flight.recordDeparture(STD.minusMinutes(20), NOW, TOLERANCE);

        assertEquals(STD.minusMinutes(20), flight.getActualDeparture());
    }

    @Test
    void departureInTheFutureIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> flight.recordDeparture(STD, STD.minusMinutes(1), TOLERANCE));
    }

    @Test
    void departureAtTheCurrentMomentIsAllowed() {
        flight.recordDeparture(STD, STD, TOLERANCE);

        assertEquals(STD, flight.getActualDeparture());
    }

    @Test
    void arrivalExactlyAtTheToleranceIsAllowed() {
        flight.recordDeparture(STD, NOW, TOLERANCE);

        flight.recordArrival(STA.plusMinutes(5), NOW, TOLERANCE);

        assertEquals(FlightStatus.LANDED, flight.getStatus());
    }

    @Test
    void arrivalLaterThanTheToleranceRequiresADelayFirst() {
        flight.recordDeparture(STD, NOW, TOLERANCE);

        assertThrows(InvalidFlightStateException.class,
                () -> flight.recordArrival(STA.plusMinutes(6), NOW, TOLERANCE));
    }

    @Test
    void arrivalAfterAnArrivalDelayIsMeasuredAgainstTheNewEta() {
        flight.recordDeparture(STD, NOW, TOLERANCE);
        flight.registerDelay(DelaySide.ARRIVAL, 20, "87", "Airport congestion");

        flight.recordArrival(STA.plusMinutes(24), NOW, TOLERANCE);

        assertEquals(STA.plusMinutes(24), flight.getActualArrival());
    }

    @Test
    void arrivalInTheFutureIsRefused() {
        flight.recordDeparture(STD, NOW, TOLERANCE);

        assertThrows(IllegalArgumentException.class,
                () -> flight.recordArrival(STA, STA.minusMinutes(1), TOLERANCE));
    }
}
