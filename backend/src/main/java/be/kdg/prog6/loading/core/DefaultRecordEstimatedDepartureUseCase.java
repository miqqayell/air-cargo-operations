package be.kdg.prog6.loading.core;

import be.kdg.prog6.loading.domain.Load;
import be.kdg.prog6.loading.port.in.RecordEstimatedDepartureCommand;
import be.kdg.prog6.loading.port.in.RecordEstimatedDepartureUseCase;
import be.kdg.prog6.loading.port.out.LoadLoadPort;
import be.kdg.prog6.loading.port.out.LoadUpdatePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DefaultRecordEstimatedDepartureUseCase implements RecordEstimatedDepartureUseCase {

    private final LoadLoadPort loadLoadPort;
    private final List<LoadUpdatePort> loadUpdatePorts;

    public DefaultRecordEstimatedDepartureUseCase(LoadLoadPort loadLoadPort, List<LoadUpdatePort> loadUpdatePorts) {
        this.loadLoadPort = loadLoadPort;
        this.loadUpdatePorts = loadUpdatePorts;
    }

    @Override
    @Transactional
    public void recordEstimatedDeparture(RecordEstimatedDepartureCommand command) {
        Load load = loadLoadPort.loadLoad(command.flightId())
                .orElseThrow(() -> new NoSuchElementException("No load for flight " + command.flightId()));

        load.estimateDeparture(command.estimatedDeparture());

        loadUpdatePorts.forEach(port -> port.updateLoad(load));
    }
}
