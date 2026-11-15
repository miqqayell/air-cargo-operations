package be.kdg.prog6.flights.core;

import be.kdg.prog6.flights.domain.FlightPolicy;
import be.kdg.prog6.flights.port.in.GetFlightPolicyQuery;
import be.kdg.prog6.flights.port.out.FlightPolicyPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultGetFlightPolicyQuery implements GetFlightPolicyQuery {

    private final FlightPolicyPort flightPolicyPort;

    public DefaultGetFlightPolicyQuery(FlightPolicyPort flightPolicyPort) {
        this.flightPolicyPort = flightPolicyPort;
    }

    @Override
    @Transactional(readOnly = true)
    public FlightPolicy getFlightPolicy() {
        return flightPolicyPort.flightPolicy();
    }
}
