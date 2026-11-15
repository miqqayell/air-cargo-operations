package be.kdg.prog6.common.domain;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.stream.Stream;

public record DayOfMonth(int day) {

    public DayOfMonth {
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("Invalid day of the month: " + day);
        }
    }

    public LocalDate nearestDateTo(LocalDate today) {
        YearMonth thisMonth = YearMonth.from(today);
        YearMonth nearest = Stream.of(thisMonth.minusMonths(1), thisMonth, thisMonth.plusMonths(1))
                .min(Comparator.comparingLong((YearMonth month) -> Math.abs(daysFrom(today, month)))
                        .thenComparing(month -> daysFrom(today, month) < 0))
                .orElseThrow();
        if (day > nearest.lengthOfMonth()) {
            throw new IllegalArgumentException("Day " + day + " does not exist in " + nearest);
        }
        return nearest.atDay(day);
    }

    private long daysFrom(LocalDate today, YearMonth month) {
        return ChronoUnit.DAYS.between(today, month.atDay(1).plusDays(day - 1L));
    }
}
