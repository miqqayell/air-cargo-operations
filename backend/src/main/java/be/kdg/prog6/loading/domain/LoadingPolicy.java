package be.kdg.prog6.loading.domain;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

public record LoadingPolicy(Duration windowOpensBeforeScheduledDeparture,
                            Duration windowClosesBeforeEstimatedDeparture,
                            BigDecimal maxLateralDifferencePercent,
                            CheckKind offloadingCheck) {

    public static final Duration MIN_WINDOW_OPENING = Duration.ofHours(1);
    public static final Duration MAX_WINDOW_OPENING = Duration.ofDays(7);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public LoadingPolicy {
        if (windowOpensBeforeScheduledDeparture == null
                || windowOpensBeforeScheduledDeparture.compareTo(MIN_WINDOW_OPENING) < 0
                || windowOpensBeforeScheduledDeparture.compareTo(MAX_WINDOW_OPENING) > 0) {
            throw new IllegalArgumentException("The loading window must open between " + MIN_WINDOW_OPENING.toHours()
                    + " and " + MAX_WINDOW_OPENING.toHours() + " hours before the scheduled departure");
        }
        if (windowClosesBeforeEstimatedDeparture == null || windowClosesBeforeEstimatedDeparture.isNegative()) {
            throw new IllegalArgumentException("The loading window cannot close after the estimated departure");
        }
        if (windowClosesBeforeEstimatedDeparture.compareTo(windowOpensBeforeScheduledDeparture) >= 0) {
            throw new IllegalArgumentException("The loading window must open earlier before departure than it closes");
        }
        if (maxLateralDifferencePercent == null || maxLateralDifferencePercent.signum() < 0
                || maxLateralDifferencePercent.compareTo(HUNDRED) > 0) {
            throw new IllegalArgumentException("The maximum lateral weight difference must be between 0 and 100%");
        }
        if (maxLateralDifferencePercent.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("The maximum lateral weight difference has at most two decimals");
        }
        if (offloadingCheck == null) {
            throw new IllegalArgumentException("The offloading check must be HARD or SOFT");
        }
    }

    public List<LoadCheck> loadChecks() {
        return List.of(
                new LateralBalanceCheck(maxLateralDifferencePercent),
                new OffloadingCheck(offloadingCheck),
                new AllCargoLoadedCheck());
    }
}
