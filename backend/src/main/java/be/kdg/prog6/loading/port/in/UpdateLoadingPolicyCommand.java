package be.kdg.prog6.loading.port.in;

import be.kdg.prog6.loading.domain.LoadingPolicy;

import java.util.Objects;

public record UpdateLoadingPolicyCommand(LoadingPolicy loadingPolicy) {

    public UpdateLoadingPolicyCommand {
        Objects.requireNonNull(loadingPolicy, "Loading policy must not be null");
    }
}
