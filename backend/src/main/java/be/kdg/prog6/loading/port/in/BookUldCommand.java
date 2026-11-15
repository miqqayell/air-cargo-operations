package be.kdg.prog6.loading.port.in;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.loading.domain.UldCode;
import be.kdg.prog6.loading.domain.Weight;

import java.util.Objects;

public record BookUldCommand(FlightId flightId, UldCode uldCode, Weight weight, AirportCode destination) {

    public BookUldCommand {
        Objects.requireNonNull(flightId, "Flight ID must not be null");
        Objects.requireNonNull(uldCode, "ULD code must not be null");
        Objects.requireNonNull(weight, "Weight must not be null");
        Objects.requireNonNull(destination, "Destination must not be null");
    }
}
