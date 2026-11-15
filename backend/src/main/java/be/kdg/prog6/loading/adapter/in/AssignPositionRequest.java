package be.kdg.prog6.loading.adapter.in;

import jakarta.validation.constraints.NotBlank;

public record AssignPositionRequest(@NotBlank String uldCode, @NotBlank String position) {
}
