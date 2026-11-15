package be.kdg.prog6.loading.port.in;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;

import java.time.LocalDateTime;
import java.util.Objects;

public record OpenLoadCommand(FlightId flightId, AirportCode destination, LocalDateTime scheduledDeparture) {

    public OpenLoadCommand {
        Objects.requireNonNull(flightId, "Flight ID must not be null");
        Objects.requireNonNull(destination, "Destination must not be null");
        Objects.requireNonNull(scheduledDeparture, "Scheduled departure must not be null");
    }
}
