package be.kdg.prog6.loading.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static be.kdg.prog6.loading.domain.LoadChecksFixture.ATH;
import static be.kdg.prog6.loading.domain.LoadChecksFixture.DECK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AllCargoLoadedCheckTest {

    private final AllCargoLoadedCheck check = new AllCargoLoadedCheck();

    @Test
    void passesWhenEveryBookedUldHasAPosition() {
        assertTrue(check.check(new LoadChecksFixture().on("CL", ATH).on("CR", ATH).load(), DECK).passed());
    }

    @Test
    void warnsAboutBookedUldsWithoutAPosition() {
        var fixture = new LoadChecksFixture().on("CL", ATH).booked(ATH);

        CheckResult result = check.check(fixture.load(), DECK);

        assertFalse(result.passed());
        assertEquals(CheckKind.SOFT, result.kind());
        assertEquals("1 booked ULD(s) have no position and stay behind: AKE10001R7", result.reason());
    }

    @Test
    void reportCollectsTheResultsOfAllChecks() {
        var policy = new LoadingPolicy(Duration.ofHours(12), Duration.ofMinutes(20), BigDecimal.TEN, CheckKind.HARD);
        Load load = new LoadChecksFixture().on("CL", ATH).booked(ATH).load();

        LoadReport report = load.check(policy.loadChecks(), DECK);

        assertEquals(3, report.results().size());
        assertFalse(report.hardChecksPass());
        assertEquals(1, report.softWarnings().size());
    }
}
