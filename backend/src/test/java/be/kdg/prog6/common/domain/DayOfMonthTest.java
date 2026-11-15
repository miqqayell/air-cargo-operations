package be.kdg.prog6.common.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DayOfMonthTest {

    private static final DayOfMonth NINTH = new DayOfMonth(9);

    @Test
    void sameDayIsToday() {
        assertEquals(LocalDate.of(2027, 1, 9), NINTH.nearestDateTo(LocalDate.of(2027, 1, 9)));
    }

    @Test
    void dayJustPassedIsInThePast() {
        assertEquals(LocalDate.of(2027, 1, 9), NINTH.nearestDateTo(LocalDate.of(2027, 1, 10)));
    }

    @Test
    void dayJustAheadIsInTheFuture() {
        assertEquals(LocalDate.of(2027, 1, 9), NINTH.nearestDateTo(LocalDate.of(2027, 1, 8)));
    }

    @Test
    void nearestDateCanBeInTheNextMonth() {
        assertEquals(LocalDate.of(2027, 2, 1), new DayOfMonth(1).nearestDateTo(LocalDate.of(2027, 1, 31)));
    }

    @Test
    void nearestDateCanBeInThePreviousMonth() {
        assertEquals(LocalDate.of(2026, 12, 31), new DayOfMonth(31).nearestDateTo(LocalDate.of(2027, 1, 1)));
    }

    @Test
    void equallyNearDatesTakeTheFutureOne() {
        assertEquals(LocalDate.of(2027, 5, 1), new DayOfMonth(1).nearestDateTo(LocalDate.of(2027, 4, 16)));
    }

    @Test
    void dayThatDoesNotExistInTheNearestMonthIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new DayOfMonth(30).nearestDateTo(LocalDate.of(2027, 3, 1)));
    }

    @Test
    void dayOutsideAnyMonthIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new DayOfMonth(32));
        assertThrows(IllegalArgumentException.class, () -> new DayOfMonth(0));
    }
}
