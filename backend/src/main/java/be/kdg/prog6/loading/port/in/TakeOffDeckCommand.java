package be.kdg.prog6.loading.port.in;

import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.loading.domain.UldCode;

import java.util.Objects;

public record TakeOffDeckCommand(FlightId flightId, UldCode uldCode) {

    public TakeOffDeckCommand {
        Objects.requireNonNull(flightId, "Flight ID must not be null");
        Objects.requireNonNull(uldCode, "ULD code must not be null");
    }
}
