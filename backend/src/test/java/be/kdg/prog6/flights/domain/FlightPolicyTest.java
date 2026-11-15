package be.kdg.prog6.flights.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FlightPolicyTest {

    private static final Duration WEEK = Duration.ofDays(7);

    @Test
    void theToleranceMayBeAnythingFromZeroToAnHour() {
        assertDoesNotThrow(() -> new FlightPolicy(Duration.ZERO, WEEK));
        assertDoesNotThrow(() -> new FlightPolicy(Duration.ofMinutes(60), WEEK));
    }

    @Test
    void aNegativeOrTooLargeToleranceIsRefused() {
        var negative = assertThrows(IllegalArgumentException.class,
                () -> new FlightPolicy(Duration.ofMinutes(-1), WEEK));
        assertThrows(IllegalArgumentException.class, () -> new FlightPolicy(Duration.ofMinutes(61), WEEK));
        assertEquals("The actual-time tolerance must be between 0 and 60 minutes", negative.getMessage());
    }

    @Test
    void theRetentionIsBetweenADayAndAYear() {
        assertDoesNotThrow(() -> new FlightPolicy(Duration.ofMinutes(5), Duration.ofDays(1)));
        assertDoesNotThrow(() -> new FlightPolicy(Duration.ofMinutes(5), Duration.ofDays(365)));
        var none = assertThrows(IllegalArgumentException.class,
                () -> new FlightPolicy(Duration.ofMinutes(5), Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new FlightPolicy(Duration.ofMinutes(5), Duration.ofDays(366)));
        assertEquals("The flight retention must be between 1 and 365 days", none.getMessage());
    }

    @Test
    void aFlightIsRetainedForTheRetentionPeriodAfterItsScheduledDepartureDate() {
        var policy = new FlightPolicy(Duration.ofMinutes(5), WEEK);

        assertEquals(LocalDate.of(2026, 10, 23), policy.oldestRetainedDate(LocalDate.of(2026, 10, 30)));
    }
}
