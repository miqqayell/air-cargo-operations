package be.kdg.prog6.flights.core;

import be.kdg.prog6.flights.domain.Flight;
import be.kdg.prog6.flights.port.in.ReceiveFinalLoadCommand;
import be.kdg.prog6.flights.port.in.ReceiveFinalLoadUseCase;
import be.kdg.prog6.flights.port.out.FlightLoadPort;
import be.kdg.prog6.flights.port.out.FlightUpdatePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DefaultReceiveFinalLoadUseCase implements ReceiveFinalLoadUseCase {

    private final FlightLoadPort flightLoadPort;
    private final List<FlightUpdatePort> flightUpdatePorts;

    public DefaultReceiveFinalLoadUseCase(FlightLoadPort flightLoadPort, List<FlightUpdatePort> flightUpdatePorts) {
        this.flightLoadPort = flightLoadPort;
        this.flightUpdatePorts = flightUpdatePorts;
    }

    @Override
    @Transactional
    public Flight receiveFinalLoad(ReceiveFinalLoadCommand command) {
        Flight flight = flightLoadPort.loadFlight(command.flightId())
                .orElseThrow(() -> new NoSuchElementException("Flight not found: " + command.flightId()));

        flight.receiveFinalLoad();

        flightUpdatePorts.forEach(port -> port.updateFlight(flight));
        return flight;
    }
}
