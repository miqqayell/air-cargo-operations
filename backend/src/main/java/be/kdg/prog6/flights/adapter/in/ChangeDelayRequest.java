package be.kdg.prog6.flights.adapter.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangeDelayRequest(@NotNull Integer minutes, @NotBlank String description) {
}
