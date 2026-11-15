package be.kdg.prog6.flights.core;

import be.kdg.prog6.flights.domain.Flight;
import be.kdg.prog6.flights.port.in.RemoveDelayCommand;
import be.kdg.prog6.flights.port.in.RemoveDelayUseCase;
import be.kdg.prog6.flights.port.out.FlightLoadPort;
import be.kdg.prog6.flights.port.out.FlightUpdatePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DefaultRemoveDelayUseCase implements RemoveDelayUseCase {

    private final FlightLoadPort flightLoadPort;
    private final List<FlightUpdatePort> flightUpdatePorts;

    public DefaultRemoveDelayUseCase(FlightLoadPort flightLoadPort, List<FlightUpdatePort> flightUpdatePorts) {
        this.flightLoadPort = flightLoadPort;
        this.flightUpdatePorts = flightUpdatePorts;
    }

    @Override
    @Transactional
    public Flight removeDelay(RemoveDelayCommand command) {
        Flight flight = flightLoadPort.loadFlight(command.flightId())
                .orElseThrow(() -> new NoSuchElementException("Flight not found: " + command.flightId()));

        flight.removeDelay(command.delayId());

        flightUpdatePorts.forEach(port -> port.updateFlight(flight));
        return flight;
    }
}
