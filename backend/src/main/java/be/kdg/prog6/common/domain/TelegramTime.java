package be.kdg.prog6.common.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

public record TelegramTime(LocalTime time) {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HHmm");

    public TelegramTime {
        Objects.requireNonNull(time, "Time is required");
    }

    public static TelegramTime parse(String text) {
        try {
            return new TelegramTime(LocalTime.parse(text, FORMAT));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid telegram time: " + text, e);
        }
    }

    public LocalDateTime nearestMomentTo(LocalDateTime reading) {
        LocalDate today = reading.toLocalDate();
        return Stream.of(today.minusDays(1), today, today.plusDays(1))
                .map(date -> date.atTime(time))
                .min(Comparator.comparing((LocalDateTime moment) -> Duration.between(reading, moment).abs())
                        .thenComparing(moment -> moment.isBefore(reading)))
                .orElseThrow();
    }
}
