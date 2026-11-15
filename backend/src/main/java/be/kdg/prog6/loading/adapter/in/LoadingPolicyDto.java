package be.kdg.prog6.loading.adapter.in;

import be.kdg.prog6.loading.domain.CheckKind;
import be.kdg.prog6.loading.domain.LoadingPolicy;

import java.math.BigDecimal;

public record LoadingPolicyDto(long windowOpensHoursBeforeStd, long windowClosesMinutesBeforeEtd,
                               BigDecimal maxLateralDifferencePercent, CheckKind offloadingCheck) {

    static LoadingPolicyDto from(LoadingPolicy loadingPolicy) {
        return new LoadingPolicyDto(
                loadingPolicy.windowOpensBeforeScheduledDeparture().toHours(),
                loadingPolicy.windowClosesBeforeEstimatedDeparture().toMinutes(),
                loadingPolicy.maxLateralDifferencePercent(),
                loadingPolicy.offloadingCheck());
    }
}
