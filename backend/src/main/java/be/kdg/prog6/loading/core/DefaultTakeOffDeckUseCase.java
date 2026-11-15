package be.kdg.prog6.loading.core;

import be.kdg.prog6.loading.domain.Load;
import be.kdg.prog6.loading.port.in.TakeOffDeckCommand;
import be.kdg.prog6.loading.port.in.TakeOffDeckUseCase;
import be.kdg.prog6.loading.port.out.LoadLoadPort;
import be.kdg.prog6.loading.port.out.LoadUpdatePort;
import be.kdg.prog6.loading.port.out.LoadingPolicyPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DefaultTakeOffDeckUseCase implements TakeOffDeckUseCase {

    private final LoadLoadPort loadLoadPort;
    private final List<LoadUpdatePort> loadUpdatePorts;
    private final LoadingPolicyPort loadingPolicyPort;
    private final Clock clock;

    public DefaultTakeOffDeckUseCase(LoadLoadPort loadLoadPort, List<LoadUpdatePort> loadUpdatePorts,
                                     LoadingPolicyPort loadingPolicyPort, Clock clock) {
        this.loadLoadPort = loadLoadPort;
        this.loadUpdatePorts = loadUpdatePorts;
        this.loadingPolicyPort = loadingPolicyPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Load takeOffDeck(TakeOffDeckCommand command) {
        Load load = loadLoadPort.loadLoad(command.flightId())
                .orElseThrow(() -> new NoSuchElementException("No load for flight " + command.flightId()));

        load.takeOffDeck(command.uldCode(), loadingPolicyPort.loadingPolicy(), LocalDateTime.now(clock));

        loadUpdatePorts.forEach(port -> port.updateLoad(load));
        return load;
    }
}
