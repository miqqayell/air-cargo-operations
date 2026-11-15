package be.kdg.prog6.common.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TelegramTimeTest {

    private static final DayOfMonth NINTH = new DayOfMonth(9);
    private static final LocalDate JANUARY_9 = LocalDate.of(2026, 1, 9);

    @Test
    void estimatedDepartureReadOnTheSameDay() {
        LocalDateTime reading = LocalDateTime.of(2026, 1, 9, 8, 0);

        assertEquals(JANUARY_9, NINTH.nearestDateTo(reading.toLocalDate()));
        assertEquals(LocalDateTime.of(2026, 1, 9, 12, 0), TelegramTime.parse("1200").nearestMomentTo(reading));
    }

    @Test
    void actualDepartureReadJustAfterMidnightWasYesterday() {
        LocalDateTime reading = LocalDateTime.of(2026, 1, 10, 0, 2);

        assertEquals(JANUARY_9, NINTH.nearestDateTo(reading.toLocalDate()));
        assertEquals(LocalDateTime.of(2026, 1, 9, 23, 55), TelegramTime.parse("2355").nearestMomentTo(reading));
    }

    @Test
    void estimatedDepartureReadJustBeforeMidnightIsTomorrow() {
        LocalDateTime reading = LocalDateTime.of(2026, 1, 8, 23, 58);

        assertEquals(JANUARY_9, NINTH.nearestDateTo(reading.toLocalDate()));
        assertEquals(LocalDateTime.of(2026, 1, 9, 0, 15), TelegramTime.parse("0015").nearestMomentTo(reading));
    }

    @Test
    void theTimeCanFallOnAnotherDateThanTheFlight() {
        LocalDateTime reading = LocalDateTime.of(2026, 1, 9, 23, 58);

        assertEquals(JANUARY_9, NINTH.nearestDateTo(reading.toLocalDate()));
        assertEquals(LocalDateTime.of(2026, 1, 10, 0, 15), TelegramTime.parse("0015").nearestMomentTo(reading));
    }

    @Test
    void equallyNearMomentsTakeTheFutureOne() {
        LocalDateTime reading = LocalDateTime.of(2026, 1, 9, 6, 0);

        assertEquals(LocalDateTime.of(2026, 1, 9, 18, 0), TelegramTime.parse("1800").nearestMomentTo(reading));
    }

    @Test
    void aTimeThatDoesNotExistIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> TelegramTime.parse("2460"));
        assertThrows(IllegalArgumentException.class, () -> TelegramTime.parse("12:00"));
    }
}
