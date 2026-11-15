package be.kdg.prog6.simulator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record FlightScheduleRequest(String flightNumber, String departureAirport, String arrivalAirport,
                                    String aircraftRegistration, LocalDate firstDate, LocalDate lastDate,
                                    Set<DayOfWeek> weekdays, LocalTime departureTime, LocalTime arrivalTime) {
}
