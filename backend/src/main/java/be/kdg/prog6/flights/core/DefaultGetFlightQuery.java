package be.kdg.prog6.flights.core;

import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.flights.domain.Flight;
import be.kdg.prog6.flights.port.in.GetFlightQuery;
import be.kdg.prog6.flights.port.out.FlightLoadPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class DefaultGetFlightQuery implements GetFlightQuery {

    private final FlightLoadPort flightLoadPort;

    public DefaultGetFlightQuery(FlightLoadPort flightLoadPort) {
        this.flightLoadPort = flightLoadPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Flight getFlight(FlightId flightId) {
        return flightLoadPort.loadFlight(flightId)
                .orElseThrow(() -> new NoSuchElementException("Flight not found: " + flightId));
    }
}
