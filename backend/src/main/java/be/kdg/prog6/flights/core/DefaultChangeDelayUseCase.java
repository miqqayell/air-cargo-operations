package be.kdg.prog6.flights.core;

import be.kdg.prog6.flights.domain.Flight;
import be.kdg.prog6.flights.port.in.ChangeDelayCommand;
import be.kdg.prog6.flights.port.in.ChangeDelayUseCase;
import be.kdg.prog6.flights.port.out.FlightLoadPort;
import be.kdg.prog6.flights.port.out.FlightUpdatePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DefaultChangeDelayUseCase implements ChangeDelayUseCase {

    private final FlightLoadPort flightLoadPort;
    private final List<FlightUpdatePort> flightUpdatePorts;

    public DefaultChangeDelayUseCase(FlightLoadPort flightLoadPort, List<FlightUpdatePort> flightUpdatePorts) {
        this.flightLoadPort = flightLoadPort;
        this.flightUpdatePorts = flightUpdatePorts;
    }

    @Override
    @Transactional
    public Flight changeDelay(ChangeDelayCommand command) {
        Flight flight = flightLoadPort.loadFlight(command.flightId())
                .orElseThrow(() -> new NoSuchElementException("Flight not found: " + command.flightId()));

        flight.changeDelay(command.delayId(), command.minutes(), command.description());

        flightUpdatePorts.forEach(port -> port.updateFlight(flight));
        return flight;
    }
}
