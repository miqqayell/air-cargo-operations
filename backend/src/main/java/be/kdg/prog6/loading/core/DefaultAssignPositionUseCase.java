package be.kdg.prog6.loading.core;

import be.kdg.prog6.loading.domain.Load;
import be.kdg.prog6.loading.port.in.AssignPositionCommand;
import be.kdg.prog6.loading.port.in.AssignPositionUseCase;
import be.kdg.prog6.loading.port.out.FloorPlanPort;
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
public class DefaultAssignPositionUseCase implements AssignPositionUseCase {

    private final LoadLoadPort loadLoadPort;
    private final List<LoadUpdatePort> loadUpdatePorts;
    private final FloorPlanPort floorPlanPort;
    private final LoadingPolicyPort loadingPolicyPort;
    private final Clock clock;

    public DefaultAssignPositionUseCase(LoadLoadPort loadLoadPort, List<LoadUpdatePort> loadUpdatePorts,
                                        FloorPlanPort floorPlanPort,
                                        LoadingPolicyPort loadingPolicyPort, Clock clock) {
        this.loadLoadPort = loadLoadPort;
        this.loadUpdatePorts = loadUpdatePorts;
        this.floorPlanPort = floorPlanPort;
        this.loadingPolicyPort = loadingPolicyPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Load assignPosition(AssignPositionCommand command) {
        Load load = loadLoadPort.loadLoad(command.flightId())
                .orElseThrow(() -> new NoSuchElementException("No load for flight " + command.flightId()));

        load.assignPosition(command.uldCode(), command.position(), floorPlanPort.floorPlanFor(command.flightId()),
                loadingPolicyPort.loadingPolicy(), LocalDateTime.now(clock));

        loadUpdatePorts.forEach(port -> port.updateLoad(load));
        return load;
    }
}
