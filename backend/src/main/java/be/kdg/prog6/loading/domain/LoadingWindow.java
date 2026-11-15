package be.kdg.prog6.loading.domain;

import java.time.LocalDateTime;

public record LoadingWindow(LocalDateTime opensAt, LocalDateTime closesAt) {

    public boolean isOpenAt(LocalDateTime moment) {
        return !moment.isBefore(opensAt) && moment.isBefore(closesAt);
    }

    void requireOpenAt(LocalDateTime moment) {
        if (moment.isBefore(opensAt)) {
            throw new IllegalStateException("The loading window opens at " + opensAt + " UTC");
        }
        if (!moment.isBefore(closesAt)) {
            throw new IllegalStateException("The loading window closed at " + closesAt + " UTC");
        }
    }
}
