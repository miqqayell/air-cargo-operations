package be.kdg.prog6.loading.port.in;

import be.kdg.prog6.common.domain.FlightId;

import java.util.Objects;

public record DeclareLoadFinalCommand(FlightId flightId, boolean softWarningsConfirmed) {

    public DeclareLoadFinalCommand {
        Objects.requireNonNull(flightId, "Flight ID must not be null");
    }
}
