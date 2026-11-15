package be.kdg.prog6.flights.domain;

import java.time.Duration;
import java.time.LocalDate;

public record FlightPolicy(Duration actualTimeTolerance, Duration retention) {

    public static final Duration MAX_ACTUAL_TIME_TOLERANCE = Duration.ofHours(1);
    public static final Duration MIN_RETENTION = Duration.ofDays(1);
    public static final Duration MAX_RETENTION = Duration.ofDays(365);

    public FlightPolicy {
        if (actualTimeTolerance == null || actualTimeTolerance.isNegative()
                || actualTimeTolerance.compareTo(MAX_ACTUAL_TIME_TOLERANCE) > 0) {
            throw new IllegalArgumentException("The actual-time tolerance must be between 0 and "
                    + MAX_ACTUAL_TIME_TOLERANCE.toMinutes() + " minutes");
        }
        if (retention == null || retention.compareTo(MIN_RETENTION) < 0 || retention.compareTo(MAX_RETENTION) > 0) {
            throw new IllegalArgumentException("The flight retention must be between " + MIN_RETENTION.toDays()
                    + " and " + MAX_RETENTION.toDays() + " days");
        }
    }

    public LocalDate oldestRetainedDate(LocalDate today) {
        return today.minusDays(retention.toDays());
    }
}
