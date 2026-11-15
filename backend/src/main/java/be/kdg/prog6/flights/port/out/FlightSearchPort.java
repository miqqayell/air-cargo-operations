package be.kdg.prog6.flights.port.out;

import be.kdg.prog6.flights.domain.Flight;
import be.kdg.prog6.flights.domain.FlightSearchCriteria;

import java.util.List;

public interface FlightSearchPort {

    List<Flight> searchFlights(FlightSearchCriteria criteria);
}
