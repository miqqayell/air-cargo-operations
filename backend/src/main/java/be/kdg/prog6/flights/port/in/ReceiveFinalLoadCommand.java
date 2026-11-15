package be.kdg.prog6.flights.port.in;

import be.kdg.prog6.common.domain.FlightId;

import java.util.Objects;

public record ReceiveFinalLoadCommand(FlightId flightId) {

    public ReceiveFinalLoadCommand {
        Objects.requireNonNull(flightId, "Flight ID must not be null");
    }
}
