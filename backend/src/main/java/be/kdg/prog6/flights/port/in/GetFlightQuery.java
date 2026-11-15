package be.kdg.prog6.flights.port.in;

import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.flights.domain.Flight;

public interface GetFlightQuery {

    Flight getFlight(FlightId flightId);
}
