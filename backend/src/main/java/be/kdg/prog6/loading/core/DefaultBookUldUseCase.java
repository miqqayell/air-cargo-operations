package be.kdg.prog6.loading.core;

import be.kdg.prog6.loading.domain.Load;
import be.kdg.prog6.loading.port.in.BookUldCommand;
import be.kdg.prog6.loading.port.in.BookUldUseCase;
import be.kdg.prog6.loading.port.out.LoadLoadPort;
import be.kdg.prog6.loading.port.out.LoadUpdatePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DefaultBookUldUseCase implements BookUldUseCase {

    private static final Logger log = LoggerFactory.getLogger(DefaultBookUldUseCase.class);

    private final LoadLoadPort loadLoadPort;
    private final List<LoadUpdatePort> loadUpdatePorts;

    public DefaultBookUldUseCase(LoadLoadPort loadLoadPort, List<LoadUpdatePort> loadUpdatePorts) {
        this.loadLoadPort = loadLoadPort;
        this.loadUpdatePorts = loadUpdatePorts;
    }

    @Override
    @Transactional
    public Load bookUld(BookUldCommand command) {
        Load load = loadLoadPort.loadLoad(command.flightId())
                .orElseThrow(() -> new NoSuchElementException("No load for flight " + command.flightId()));

        boolean booked = load.bookUld(command.uldCode(), command.weight(), command.destination());
        if (!booked) {
            log.warn("ULD {} is already booked on flight {}, booking ignored", command.uldCode().value(), command.flightId());
            return load;
        }

        loadUpdatePorts.forEach(port -> port.updateLoad(load));
        return load;
    }
}
