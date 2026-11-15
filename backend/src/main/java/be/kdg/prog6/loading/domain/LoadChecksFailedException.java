package be.kdg.prog6.loading.domain;

import java.util.List;
import java.util.stream.Collectors;

public class LoadChecksFailedException extends IllegalStateException {

    public LoadChecksFailedException(String message, List<CheckResult> results) {
        super(results.stream()
                .map(result -> result.name() + ": " + result.reason())
                .collect(Collectors.joining(". ", message + ". ", "")));
    }
}
