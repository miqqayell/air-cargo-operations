package be.kdg.prog6.loading.core;

import be.kdg.prog6.loading.port.in.RemoveLoadCommand;
import be.kdg.prog6.loading.port.in.RemoveLoadUseCase;
import be.kdg.prog6.loading.port.out.LoadRemovePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultRemoveLoadUseCase implements RemoveLoadUseCase {

    private static final Logger log = LoggerFactory.getLogger(DefaultRemoveLoadUseCase.class);

    private final LoadRemovePort loadRemovePort;

    public DefaultRemoveLoadUseCase(LoadRemovePort loadRemovePort) {
        this.loadRemovePort = loadRemovePort;
    }

    @Override
    @Transactional
    public void removeLoad(RemoveLoadCommand command) {
        loadRemovePort.removeLoad(command.flightId());
        log.debug("Removed the load of flight {}", command.flightId());
    }
}
