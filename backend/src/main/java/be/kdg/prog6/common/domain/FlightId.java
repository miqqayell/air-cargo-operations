package be.kdg.prog6.common.domain;

import java.time.LocalDate;
import java.util.Objects;

public record FlightId(FlightNumber flightNumber, LocalDate scheduledDepartureDate, AirportCode departureAirport) {

    public FlightId {
        Objects.requireNonNull(flightNumber, "Flight number must not be null");
        Objects.requireNonNull(scheduledDepartureDate, "Scheduled departure date must not be null");
        Objects.requireNonNull(departureAirport, "Departure airport must not be null");
    }
}
