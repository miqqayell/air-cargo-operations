package be.kdg.prog6.flights.core;

import be.kdg.prog6.flights.domain.Flight;
import be.kdg.prog6.flights.domain.FlightPolicy;
import be.kdg.prog6.flights.port.in.RemoveExpiredFlightsUseCase;
import be.kdg.prog6.flights.port.out.FlightLoadPort;
import be.kdg.prog6.flights.port.out.FlightPolicyPort;
import be.kdg.prog6.flights.port.out.FlightRemovePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class DefaultRemoveExpiredFlightsUseCase implements RemoveExpiredFlightsUseCase {

    private static final Logger log = LoggerFactory.getLogger(DefaultRemoveExpiredFlightsUseCase.class);

    private final FlightPolicyPort flightPolicyPort;
    private final FlightLoadPort flightLoadPort;
    private final List<FlightRemovePort> flightRemovePorts;
    private final Clock clock;

    public DefaultRemoveExpiredFlightsUseCase(FlightPolicyPort flightPolicyPort, FlightLoadPort flightLoadPort,
                                              List<FlightRemovePort> flightRemovePorts, Clock clock) {
        this.flightPolicyPort = flightPolicyPort;
        this.flightLoadPort = flightLoadPort;
        this.flightRemovePorts = flightRemovePorts;
        this.clock = clock;
    }

    @Override
    @Transactional
    public int removeExpiredFlights() {
        FlightPolicy policy = flightPolicyPort.flightPolicy();
        LocalDate oldestRetainedDate = policy.oldestRetainedDate(LocalDate.now(clock));
        List<Flight> expiredFlights = flightLoadPort.loadFlightsScheduledBefore(oldestRetainedDate);

        for (Flight flight : expiredFlights) {
            flight.remove();
            flightRemovePorts.forEach(port -> port.removeFlight(flight));
            log.debug("Removed flight {}", flight.getId());
        }
        if (!expiredFlights.isEmpty()) {
            log.info("Removed {} flight(s) scheduled before {}, the retention is {} days",
                    expiredFlights.size(), oldestRetainedDate, policy.retention().toDays());
        }
        return expiredFlights.size();
    }
}
