package be.kdg.prog6.flights.adapter.in;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record ActualTimeRequest(@NotNull Instant time) {

    LocalDateTime utc() {
        return LocalDateTime.ofInstant(time, ZoneOffset.UTC);
    }
}
