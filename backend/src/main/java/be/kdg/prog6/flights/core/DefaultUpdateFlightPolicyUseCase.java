package be.kdg.prog6.flights.core;

import be.kdg.prog6.flights.domain.FlightPolicy;
import be.kdg.prog6.flights.port.in.UpdateFlightPolicyCommand;
import be.kdg.prog6.flights.port.in.UpdateFlightPolicyUseCase;
import be.kdg.prog6.flights.port.out.FlightPolicyPort;
import be.kdg.prog6.flights.port.out.UpdateFlightPolicyPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultUpdateFlightPolicyUseCase implements UpdateFlightPolicyUseCase {

    private static final Logger log = LoggerFactory.getLogger(DefaultUpdateFlightPolicyUseCase.class);

    private final FlightPolicyPort flightPolicyPort;
    private final UpdateFlightPolicyPort updateFlightPolicyPort;

    public DefaultUpdateFlightPolicyUseCase(FlightPolicyPort flightPolicyPort,
                                            UpdateFlightPolicyPort updateFlightPolicyPort) {
        this.flightPolicyPort = flightPolicyPort;
        this.updateFlightPolicyPort = updateFlightPolicyPort;
    }

    @Override
    @Transactional
    public FlightPolicy updateFlightPolicy(UpdateFlightPolicyCommand command) {
        FlightPolicy previous = flightPolicyPort.flightPolicy();
        updateFlightPolicyPort.updateFlightPolicy(command.flightPolicy());
        log.info("Flight policy changed from {} to {}", previous, command.flightPolicy());
        return command.flightPolicy();
    }
}
