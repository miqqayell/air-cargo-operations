package be.kdg.prog6.flights.port.in;

import be.kdg.prog6.flights.domain.Flight;

import java.util.List;

public interface ScheduleFlightsUseCase {

    List<Flight> scheduleFlights(ScheduleFlightsCommand command);
}
