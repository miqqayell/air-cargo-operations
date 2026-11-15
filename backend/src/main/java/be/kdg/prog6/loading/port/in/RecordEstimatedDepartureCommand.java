package be.kdg.prog6.loading.port.in;

import be.kdg.prog6.common.domain.FlightId;

import java.time.LocalDateTime;
import java.util.Objects;

public record RecordEstimatedDepartureCommand(FlightId flightId, LocalDateTime estimatedDeparture) {

    public RecordEstimatedDepartureCommand {
        Objects.requireNonNull(flightId, "Flight ID must not be null");
        Objects.requireNonNull(estimatedDeparture, "Estimated departure must not be null");
    }
}
