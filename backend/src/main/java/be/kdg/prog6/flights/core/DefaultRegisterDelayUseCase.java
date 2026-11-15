package be.kdg.prog6.flights.core;

import be.kdg.prog6.flights.domain.Flight;
import be.kdg.prog6.flights.port.in.RegisterDelayCommand;
import be.kdg.prog6.flights.port.in.RegisterDelayUseCase;
import be.kdg.prog6.flights.port.out.FlightLoadPort;
import be.kdg.prog6.flights.port.out.FlightUpdatePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DefaultRegisterDelayUseCase implements RegisterDelayUseCase {

    private final FlightLoadPort flightLoadPort;
    private final List<FlightUpdatePort> flightUpdatePorts;

    public DefaultRegisterDelayUseCase(FlightLoadPort flightLoadPort, List<FlightUpdatePort> flightUpdatePorts) {
        this.flightLoadPort = flightLoadPort;
        this.flightUpdatePorts = flightUpdatePorts;
    }

    @Override
    @Transactional
    public Flight registerDelay(RegisterDelayCommand command) {
        Flight flight = flightLoadPort.loadFlight(command.flightId())
                .orElseThrow(() -> new NoSuchElementException("Flight not found: " + command.flightId()));

        flight.registerDelay(command.side(), command.minutes(), command.delayCode(), command.description());

        flightUpdatePorts.forEach(port -> port.updateFlight(flight));
        return flight;
    }
}
