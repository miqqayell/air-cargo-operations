package be.kdg.prog6.flights.domain;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightNumber;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public record FlightSchedule(FlightNumber flightNumber, AirportCode departureAirport, AirportCode arrivalAirport,
                             AircraftRegistration aircraftRegistration, LocalDate firstDate, LocalDate lastDate,
                             Set<DayOfWeek> weekdays, LocalTime departureTime, LocalTime arrivalTime) {

    public FlightSchedule {
        if (lastDate.isBefore(firstDate)) {
            throw new IllegalArgumentException("The period cannot end before it starts");
        }
    }

    public List<Flight> createFlights() {
        return firstDate.datesUntil(lastDate.plusDays(1))
                .filter(date -> weekdays.contains(date.getDayOfWeek()))
                .map(this::createFlightOn)
                .toList();
    }

    private Flight createFlightOn(LocalDate date) {
        LocalDateTime departure = date.atTime(departureTime);
        LocalDate arrivalDate = arrivalTime.isAfter(departureTime) ? date : date.plusDays(1);
        LocalDateTime arrival = arrivalDate.atTime(arrivalTime);
        return Flight.schedule(flightNumber, departureAirport, arrivalAirport, aircraftRegistration, departure, arrival);
    }
}
