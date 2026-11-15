package be.kdg.prog6.flights.port.in;

import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.flights.domain.DelayId;

import java.util.Objects;

public record RemoveDelayCommand(FlightId flightId, DelayId delayId) {

    public RemoveDelayCommand {
        Objects.requireNonNull(flightId, "Flight ID must not be null");
        Objects.requireNonNull(delayId, "Delay ID must not be null");
    }
}
