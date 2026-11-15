package be.kdg.prog6.loading.domain;

import java.util.List;

public record CheckResult(String name, CheckKind kind, boolean passed, String reason, List<BlockedUld> blockedUlds) {

    static CheckResult pass(String name, CheckKind kind) {
        return new CheckResult(name, kind, true, null, List.of());
    }

    static CheckResult fail(String name, CheckKind kind, String reason) {
        return new CheckResult(name, kind, false, reason, List.of());
    }

    static CheckResult fail(String name, CheckKind kind, String reason, List<BlockedUld> blockedUlds) {
        return new CheckResult(name, kind, false, reason, List.copyOf(blockedUlds));
    }
}
