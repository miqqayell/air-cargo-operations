package be.kdg.prog6.flights.domain;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightNumber;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlightScheduleTest {

    private static FlightSchedule schedule(LocalTime departure, LocalTime arrival) {
        return new FlightSchedule(new FlightNumber("HJ221"), new AirportCode("BRU"), new AirportCode("ATH"),
                new AircraftRegistration("SXBAA"), LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 18),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), departure, arrival);
    }

    @Test
    void createsOneFlightPerMatchingWeekday() {
        List<Flight> flights = schedule(LocalTime.of(6, 30), LocalTime.of(10, 0)).createFlights();

        assertEquals(List.of(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 7),
                        LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 14)),
                flights.stream().map(flight -> flight.getId().scheduledDepartureDate()).toList());
    }

    @Test
    void arrivalBeforeDepartureTimeLandsTheNextDay() {
        Flight flight = schedule(LocalTime.of(22, 0), LocalTime.of(2, 0)).createFlights().getFirst();

        assertEquals(LocalDateTime.of(2026, 10, 6, 2, 0), flight.getScheduledArrival());
    }
}
