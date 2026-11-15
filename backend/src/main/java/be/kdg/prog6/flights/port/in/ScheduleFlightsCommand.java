package be.kdg.prog6.flights.port.in;

import be.kdg.prog6.flights.domain.FlightSchedule;

import java.util.Objects;

public record ScheduleFlightsCommand(FlightSchedule schedule) {

    public ScheduleFlightsCommand {
        Objects.requireNonNull(schedule, "Schedule must not be null");
    }
}
