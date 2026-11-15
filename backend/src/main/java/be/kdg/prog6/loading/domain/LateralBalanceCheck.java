package be.kdg.prog6.loading.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class LateralBalanceCheck implements LoadCheck {

    public static final String NAME = "Lateral balance";
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final BigDecimal maxDifferencePercent;

    public LateralBalanceCheck(BigDecimal maxDifferencePercent) {
        this.maxDifferencePercent = maxDifferencePercent;
    }

    @Override
    public CheckResult check(Load load, FloorPlan floorPlan) {
        BigDecimal left = totalOn(DeckSide.LEFT, load, floorPlan);
        BigDecimal right = totalOn(DeckSide.RIGHT, load, floorPlan);
        BigDecimal heavier = left.max(right);
        BigDecimal lighter = left.min(right);
        BigDecimal factor = HUNDRED.add(maxDifferencePercent);
        if (heavier.multiply(HUNDRED).compareTo(lighter.multiply(factor)) <= 0) {
            return CheckResult.pass(NAME, CheckKind.HARD);
        }
        BigDecimal maximum = lighter.multiply(factor).divide(HUNDRED);
        BigDecimal toMove = heavier.multiply(HUNDRED).subtract(lighter.multiply(factor))
                .divide(HUNDRED.add(factor), 1, RoundingMode.CEILING);
        String heavierSide = left.compareTo(right) > 0 ? "left" : "right";
        String lighterSide = left.compareTo(right) > 0 ? "right" : "left";
        return CheckResult.fail(NAME, CheckKind.HARD,
                ("Left side %s kg, right side %s kg: the %s side may carry at most %s kg (%s%% more than the other side). "
                        + "Move at least %s kg of cargo from the %s side to the %s side, or add cargo on the %s side.")
                        .formatted(kilograms(left), kilograms(right), heavierSide, kilograms(maximum),
                                maxDifferencePercent.stripTrailingZeros().toPlainString(),
                                kilograms(toMove), heavierSide, lighterSide, lighterSide));
    }

    private static BigDecimal totalOn(DeckSide side, Load load, FloorPlan floorPlan) {
        return load.getUlds().stream()
                .filter(uld -> uld.isPlaced() && floorPlan.contains(uld.getPosition()))
                .filter(uld -> floorPlan.sideOf(uld.getPosition()) == side)
                .map(uld -> uld.getWeight().inKilograms())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String kilograms(BigDecimal value) {
        return value.setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
