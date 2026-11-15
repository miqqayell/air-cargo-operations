package be.kdg.prog6.flights.core;

import be.kdg.prog6.flights.domain.Flight;
import be.kdg.prog6.flights.domain.FlightSearchCriteria;
import be.kdg.prog6.flights.port.in.ListFlightsQuery;
import be.kdg.prog6.flights.port.out.FlightSearchPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class DefaultListFlightsQuery implements ListFlightsQuery {

    private final FlightSearchPort flightSearchPort;
    private final Clock clock;

    public DefaultListFlightsQuery(FlightSearchPort flightSearchPort, Clock clock) {
        this.flightSearchPort = flightSearchPort;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> listFlights(FlightSearchCriteria criteria) {
        FlightSearchCriteria effective = criteria.isEmpty()
                ? FlightSearchCriteria.onDate(LocalDate.now(clock))
                : criteria;
        return flightSearchPort.searchFlights(effective);
    }
}
