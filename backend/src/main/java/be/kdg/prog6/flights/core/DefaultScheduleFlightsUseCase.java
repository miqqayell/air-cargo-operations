package be.kdg.prog6.flights.core;

import be.kdg.prog6.flights.domain.Flight;
import be.kdg.prog6.flights.port.in.ScheduleFlightsCommand;
import be.kdg.prog6.flights.port.in.ScheduleFlightsUseCase;
import be.kdg.prog6.flights.port.out.FlightLoadPort;
import be.kdg.prog6.flights.port.out.FlightUpdatePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultScheduleFlightsUseCase implements ScheduleFlightsUseCase {

    private static final Logger log = LoggerFactory.getLogger(DefaultScheduleFlightsUseCase.class);

    private final FlightLoadPort flightLoadPort;
    private final List<FlightUpdatePort> flightUpdatePorts;

    public DefaultScheduleFlightsUseCase(FlightLoadPort flightLoadPort, List<FlightUpdatePort> flightUpdatePorts) {
        this.flightLoadPort = flightLoadPort;
        this.flightUpdatePorts = flightUpdatePorts;
    }

    @Override
    @Transactional
    public List<Flight> scheduleFlights(ScheduleFlightsCommand command) {
        List<Flight> created = new ArrayList<>();
        for (Flight flight : command.schedule().createFlights()) {
            if (flightLoadPort.loadFlight(flight.getId()).isPresent()) {
                log.warn("Flight {} already exists and is not created again", flight.getId());
                continue;
            }
            flightUpdatePorts.forEach(port -> port.updateFlight(flight));
            created.add(flight);
        }
        return created;
    }
}
