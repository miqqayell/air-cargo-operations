package be.kdg.prog6.flights.port.out;

import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.flights.domain.Flight;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FlightLoadPort {

    Optional<Flight> loadFlight(FlightId flightId);

    List<Flight> loadFlightsScheduledBefore(LocalDate date);
}
