package be.kdg.prog6.loading.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static be.kdg.prog6.loading.domain.LoadChecksFixture.ATH;
import static be.kdg.prog6.loading.domain.LoadChecksFixture.DECK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LateralBalanceCheckTest {

    private final LateralBalanceCheck check = new LateralBalanceCheck(BigDecimal.TEN);

    private static Weight kg(double value) {
        return new Weight(value, WeightUnit.KG);
    }

    @Test
    void differenceExactlyAtTheLimitPasses() {
        var fixture = new LoadChecksFixture().on("CL", ATH, kg(1000)).on("CR", ATH, kg(1100));

        assertTrue(check.check(fixture.load(), DECK).passed());
    }

    @Test
    void differenceJustAboveTheLimitFails() {
        var fixture = new LoadChecksFixture().on("CL", ATH, kg(1000)).on("CR", ATH, kg(1101));

        CheckResult result = check.check(fixture.load(), DECK);

        assertFalse(result.passed());
        assertEquals(CheckKind.HARD, result.kind());
        assertEquals("Left side 1000 kg, right side 1101 kg: the right side may carry at most 1100 kg "
                + "(10% more than the other side). Move at least 0.5 kg of cargo from the right side to the left side, "
                + "or add cargo on the left side.",
                result.reason());
    }

    @Test
    void movingTheSuggestedWeightBalancesTheLoad() {
        var unbalanced = new LoadChecksFixture().on("CL", ATH, kg(3000)).on("CR", ATH, kg(1000));
        var moved = new LoadChecksFixture().on("CL", ATH, kg(2095.2)).on("CR", ATH, kg(1904.8));

        assertTrue(check.check(unbalanced.load(), DECK).reason()
                .endsWith("Move at least 904.8 kg of cargo from the left side to the right side, or add cargo on the right side."));
        assertTrue(check.check(moved.load(), DECK).passed());
    }

    @Test
    void weightsOnTheSameSideAreAddedUp() {
        var fixture = new LoadChecksFixture()
                .on("CL", ATH, kg(600)).on("DL", ATH, kg(500))
                .on("CR", ATH, kg(1000));

        assertTrue(check.check(fixture.load(), DECK).passed());
    }

    @Test
    void poundsAreConvertedToKilogramsFirst() {
        var fixture = new LoadChecksFixture()
                .on("CL", ATH, kg(1000))
                .on("CR", ATH, new Weight(2425, WeightUnit.LB));

        assertTrue(check.check(fixture.load(), DECK).passed());
    }

    @Test
    void noseAndTailCountForNeitherSide() {
        var fixture = new LoadChecksFixture()
                .on("CL", ATH, kg(1000)).on("CR", ATH, kg(1000))
                .on("A1", ATH, kg(5000)).on("X", ATH, kg(5000));

        assertTrue(check.check(fixture.load(), DECK).passed());
    }

    @Test
    void cargoOnOneSideOnlyFails() {
        var fixture = new LoadChecksFixture().on("CL", ATH, kg(500));

        assertFalse(check.check(fixture.load(), DECK).passed());
    }

    @Test
    void emptyDeckIsBalanced() {
        assertTrue(check.check(new LoadChecksFixture().booked(ATH).load(), DECK).passed());
    }
}
