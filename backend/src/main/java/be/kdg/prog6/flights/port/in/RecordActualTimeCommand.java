package be.kdg.prog6.flights.port.in;

import be.kdg.prog6.common.domain.FlightId;

import java.time.LocalDateTime;
import java.util.Objects;

public record RecordActualTimeCommand(FlightId flightId, LocalDateTime time) {

    public RecordActualTimeCommand {
        Objects.requireNonNull(flightId, "Flight ID must not be null");
        Objects.requireNonNull(time, "Time must not be null");
    }
}
