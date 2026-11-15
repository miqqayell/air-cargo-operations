package be.kdg.prog6.loading.domain;

import java.util.List;

public record LoadReport(List<CheckResult> results) {

    public LoadReport {
        results = List.copyOf(results);
    }

    public List<CheckResult> failedHardChecks() {
        return results.stream().filter(result -> !result.passed() && result.kind() == CheckKind.HARD).toList();
    }

    public List<CheckResult> softWarnings() {
        return results.stream().filter(result -> !result.passed() && result.kind() == CheckKind.SOFT).toList();
    }

    public boolean hardChecksPass() {
        return failedHardChecks().isEmpty();
    }
}
