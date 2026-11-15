package be.kdg.prog6.flights.adapter.in;

import be.kdg.prog6.flights.domain.DelaySide;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterDelayRequest(@NotNull DelaySide side,
                                   @NotNull Integer minutes,
                                   @NotBlank String delayCode,
                                   @NotBlank String description) {
}
