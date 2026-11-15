package be.kdg.prog6.loading.core;

import be.kdg.prog6.loading.domain.LoadingPolicy;
import be.kdg.prog6.loading.port.in.GetLoadingPolicyQuery;
import be.kdg.prog6.loading.port.out.LoadingPolicyPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultGetLoadingPolicyQuery implements GetLoadingPolicyQuery {

    private final LoadingPolicyPort loadingPolicyPort;

    public DefaultGetLoadingPolicyQuery(LoadingPolicyPort loadingPolicyPort) {
        this.loadingPolicyPort = loadingPolicyPort;
    }

    @Override
    @Transactional(readOnly = true)
    public LoadingPolicy getLoadingPolicy() {
        return loadingPolicyPort.loadingPolicy();
    }
}
