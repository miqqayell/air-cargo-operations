package be.kdg.prog6.loading.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoadingPolicyTest {

    private static final Duration TWELVE_HOURS = Duration.ofHours(12);
    private static final Duration TWENTY_MINUTES = Duration.ofMinutes(20);
    private static final BigDecimal TEN = BigDecimal.TEN;

    @Test
    void theDefaultsAreValid() {
        assertDoesNotThrow(() -> new LoadingPolicy(TWELVE_HOURS, TWENTY_MINUTES, TEN, CheckKind.HARD));
    }

    @Test
    void theWindowOpensBetweenOneHourAndAWeekBeforeDeparture() {
        assertDoesNotThrow(() -> new LoadingPolicy(Duration.ofHours(1), TWENTY_MINUTES, TEN, CheckKind.HARD));
        assertDoesNotThrow(() -> new LoadingPolicy(Duration.ofHours(168), TWENTY_MINUTES, TEN, CheckKind.HARD));
        var tooLate = assertThrows(IllegalArgumentException.class,
                () -> new LoadingPolicy(Duration.ofMinutes(30), Duration.ZERO, TEN, CheckKind.HARD));
        assertThrows(IllegalArgumentException.class,
                () -> new LoadingPolicy(Duration.ofHours(169), TWENTY_MINUTES, TEN, CheckKind.HARD));
        assertEquals("The loading window must open between 1 and 168 hours before the scheduled departure",
                tooLate.getMessage());
    }

    @Test
    void theWindowClosesAtTheLatestAtDepartureAndAfterItOpens() {
        assertDoesNotThrow(() -> new LoadingPolicy(TWELVE_HOURS, Duration.ZERO, TEN, CheckKind.HARD));
        assertThrows(IllegalArgumentException.class,
                () -> new LoadingPolicy(TWELVE_HOURS, Duration.ofMinutes(-5), TEN, CheckKind.HARD));
        var closedBeforeOpen = assertThrows(IllegalArgumentException.class,
                () -> new LoadingPolicy(Duration.ofHours(2), Duration.ofHours(2), TEN, CheckKind.HARD));
        assertEquals("The loading window must open earlier before departure than it closes", closedBeforeOpen.getMessage());
    }

    @Test
    void theLateralDifferenceIsAPercentageWithAtMostTwoDecimals() {
        assertDoesNotThrow(() -> new LoadingPolicy(TWELVE_HOURS, TWENTY_MINUTES, BigDecimal.ZERO, CheckKind.HARD));
        assertDoesNotThrow(() -> new LoadingPolicy(TWELVE_HOURS, TWENTY_MINUTES, new BigDecimal("7.50"), CheckKind.HARD));
        assertThrows(IllegalArgumentException.class,
                () -> new LoadingPolicy(TWELVE_HOURS, TWENTY_MINUTES, new BigDecimal("-1"), CheckKind.HARD));
        assertThrows(IllegalArgumentException.class,
                () -> new LoadingPolicy(TWELVE_HOURS, TWENTY_MINUTES, new BigDecimal("100.01"), CheckKind.HARD));
        assertThrows(IllegalArgumentException.class,
                () -> new LoadingPolicy(TWELVE_HOURS, TWENTY_MINUTES, new BigDecimal("7.125"), CheckKind.HARD));
    }

    @Test
    void theOffloadingCheckIsHardOrSoft() {
        var missing = assertThrows(IllegalArgumentException.class,
                () -> new LoadingPolicy(TWELVE_HOURS, TWENTY_MINUTES, TEN, null));
        assertEquals("The offloading check must be HARD or SOFT", missing.getMessage());
    }
}
