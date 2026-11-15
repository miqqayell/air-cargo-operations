package be.kdg.prog6.loading.core;

import be.kdg.prog6.loading.domain.LoadingPolicy;
import be.kdg.prog6.loading.port.in.UpdateLoadingPolicyCommand;
import be.kdg.prog6.loading.port.in.UpdateLoadingPolicyUseCase;
import be.kdg.prog6.loading.port.out.LoadingPolicyPort;
import be.kdg.prog6.loading.port.out.UpdateLoadingPolicyPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultUpdateLoadingPolicyUseCase implements UpdateLoadingPolicyUseCase {

    private static final Logger log = LoggerFactory.getLogger(DefaultUpdateLoadingPolicyUseCase.class);

    private final LoadingPolicyPort loadingPolicyPort;
    private final UpdateLoadingPolicyPort updateLoadingPolicyPort;

    public DefaultUpdateLoadingPolicyUseCase(LoadingPolicyPort loadingPolicyPort,
                                             UpdateLoadingPolicyPort updateLoadingPolicyPort) {
        this.loadingPolicyPort = loadingPolicyPort;
        this.updateLoadingPolicyPort = updateLoadingPolicyPort;
    }

    @Override
    @Transactional
    public LoadingPolicy updateLoadingPolicy(UpdateLoadingPolicyCommand command) {
        LoadingPolicy previous = loadingPolicyPort.loadingPolicy();
        updateLoadingPolicyPort.updateLoadingPolicy(command.loadingPolicy());
        log.info("Loading policy changed from {} to {}", previous, command.loadingPolicy());
        return command.loadingPolicy();
    }
}
