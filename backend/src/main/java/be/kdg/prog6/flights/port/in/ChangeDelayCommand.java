package be.kdg.prog6.flights.port.in;

import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.flights.domain.DelayId;

import java.util.Objects;

public record ChangeDelayCommand(FlightId flightId, DelayId delayId, int minutes, String description) {

    public ChangeDelayCommand {
        Objects.requireNonNull(flightId, "Flight ID must not be null");
        Objects.requireNonNull(delayId, "Delay ID must not be null");
        Objects.requireNonNull(description, "Description must not be null");
    }
}
