package be.kdg.prog6.flights.domain;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightNumber;

import java.time.LocalDate;

public record FlightSearchCriteria(FlightNumber flightNumber, LocalDate scheduledDepartureDate,
                                   AirportCode departureAirport, AirportCode arrivalAirport, FlightStatus status) {

    public static FlightSearchCriteria onDate(LocalDate scheduledDepartureDate) {
        return new FlightSearchCriteria(null, scheduledDepartureDate, null, null, null);
    }

    public boolean isEmpty() {
        return flightNumber == null && scheduledDepartureDate == null && departureAirport == null
                && arrivalAirport == null && status == null;
    }
}
