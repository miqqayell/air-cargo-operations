package be.kdg.prog6.loading.adapter.in;

import be.kdg.prog6.loading.domain.CheckKind;
import be.kdg.prog6.loading.domain.LoadingPolicy;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Duration;

public record LoadingPolicyRequest(@NotNull Integer windowOpensHoursBeforeStd,
                                   @NotNull Integer windowClosesMinutesBeforeEtd,
                                   @NotNull BigDecimal maxLateralDifferencePercent,
                                   @NotNull CheckKind offloadingCheck) {

    LoadingPolicy toLoadingPolicy() {
        return new LoadingPolicy(Duration.ofHours(windowOpensHoursBeforeStd),
                Duration.ofMinutes(windowClosesMinutesBeforeEtd), maxLateralDifferencePercent, offloadingCheck);
    }
}
