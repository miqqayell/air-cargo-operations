package be.kdg.prog6.flights.port.in;

import be.kdg.prog6.flights.domain.Flight;
import be.kdg.prog6.flights.domain.FlightSearchCriteria;

import java.util.List;

public interface ListFlightsQuery {

    List<Flight> listFlights(FlightSearchCriteria criteria);
}
