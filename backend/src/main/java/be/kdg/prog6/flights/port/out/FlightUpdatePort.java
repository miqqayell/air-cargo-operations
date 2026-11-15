package be.kdg.prog6.flights.port.out;

import be.kdg.prog6.flights.domain.Flight;

public interface FlightUpdatePort {

    void updateFlight(Flight flight);
}
