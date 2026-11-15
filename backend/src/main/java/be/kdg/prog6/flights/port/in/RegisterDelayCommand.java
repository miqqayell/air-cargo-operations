package be.kdg.prog6.flights.port.in;

import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.flights.domain.DelaySide;

import java.util.Objects;

public record RegisterDelayCommand(FlightId flightId, DelaySide side, int minutes, String delayCode,
                                   String description) {

    public RegisterDelayCommand {
        Objects.requireNonNull(flightId, "Flight ID must not be null");
        Objects.requireNonNull(side, "Side must not be null");
        Objects.requireNonNull(delayCode, "Delay code must not be null");
        Objects.requireNonNull(description, "Description must not be null");
    }
}
