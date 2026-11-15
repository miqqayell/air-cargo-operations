package be.kdg.prog6.loading.core;

import be.kdg.prog6.loading.domain.Load;
import be.kdg.prog6.loading.port.in.MarkLoadDepartedCommand;
import be.kdg.prog6.loading.port.in.MarkLoadDepartedUseCase;
import be.kdg.prog6.loading.port.out.LoadLoadPort;
import be.kdg.prog6.loading.port.out.LoadUpdatePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DefaultMarkLoadDepartedUseCase implements MarkLoadDepartedUseCase {

    private final LoadLoadPort loadLoadPort;
    private final List<LoadUpdatePort> loadUpdatePorts;

    public DefaultMarkLoadDepartedUseCase(LoadLoadPort loadLoadPort, List<LoadUpdatePort> loadUpdatePorts) {
        this.loadLoadPort = loadLoadPort;
        this.loadUpdatePorts = loadUpdatePorts;
    }

    @Override
    @Transactional
    public void markLoadDeparted(MarkLoadDepartedCommand command) {
        Load load = loadLoadPort.loadLoad(command.flightId())
                .orElseThrow(() -> new NoSuchElementException("No load for flight " + command.flightId()));

        load.markDeparted();

        loadUpdatePorts.forEach(port -> port.updateLoad(load));
    }
}
