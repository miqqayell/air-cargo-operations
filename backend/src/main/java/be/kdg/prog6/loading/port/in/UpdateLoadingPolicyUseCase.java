package be.kdg.prog6.loading.port.in;

import be.kdg.prog6.loading.domain.LoadingPolicy;

public interface UpdateLoadingPolicyUseCase {

    LoadingPolicy updateLoadingPolicy(UpdateLoadingPolicyCommand command);
}
