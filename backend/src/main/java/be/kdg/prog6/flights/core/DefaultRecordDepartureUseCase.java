package be.kdg.prog6.flights.core;

import be.kdg.prog6.flights.domain.Flight;
import be.kdg.prog6.flights.port.in.RecordActualTimeCommand;
import be.kdg.prog6.flights.port.in.RecordDepartureUseCase;
import be.kdg.prog6.flights.port.out.FlightLoadPort;
import be.kdg.prog6.flights.port.out.FlightPolicyPort;
import be.kdg.prog6.flights.port.out.FlightUpdatePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DefaultRecordDepartureUseCase implements RecordDepartureUseCase {

    private final FlightLoadPort flightLoadPort;
    private final List<FlightUpdatePort> flightUpdatePorts;
    private final FlightPolicyPort flightPolicyPort;
    private final Clock clock;

    public DefaultRecordDepartureUseCase(FlightLoadPort flightLoadPort, List<FlightUpdatePort> flightUpdatePorts,
                                         FlightPolicyPort flightPolicyPort, Clock clock) {
        this.flightLoadPort = flightLoadPort;
        this.flightUpdatePorts = flightUpdatePorts;
        this.flightPolicyPort = flightPolicyPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Flight recordDeparture(RecordActualTimeCommand command) {
        Flight flight = flightLoadPort.loadFlight(command.flightId())
                .orElseThrow(() -> new NoSuchElementException("Flight not found: " + command.flightId()));

        flight.recordDeparture(command.time(), LocalDateTime.now(clock), flightPolicyPort.flightPolicy().actualTimeTolerance());

        flightUpdatePorts.forEach(port -> port.updateFlight(flight));
        return flight;
    }
}
