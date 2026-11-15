package be.kdg.prog6.loading.core;

import be.kdg.prog6.loading.domain.Load;
import be.kdg.prog6.loading.port.in.OpenLoadCommand;
import be.kdg.prog6.loading.port.in.OpenLoadUseCase;
import be.kdg.prog6.loading.port.out.LoadUpdatePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DefaultOpenLoadUseCase implements OpenLoadUseCase {

    private final List<LoadUpdatePort> loadUpdatePorts;

    public DefaultOpenLoadUseCase(List<LoadUpdatePort> loadUpdatePorts) {
        this.loadUpdatePorts = loadUpdatePorts;
    }

    @Override
    @Transactional
    public Load openLoad(OpenLoadCommand command) {
        Load load = Load.openFor(command.flightId(), command.destination(), command.scheduledDeparture());
        loadUpdatePorts.forEach(port -> port.updateLoad(load));
        return load;
    }
}
