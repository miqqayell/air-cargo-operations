package be.kdg.prog6.flights.port.in;

import be.kdg.prog6.flights.domain.FlightPolicy;

import java.util.Objects;

public record UpdateFlightPolicyCommand(FlightPolicy flightPolicy) {

    public UpdateFlightPolicyCommand {
        Objects.requireNonNull(flightPolicy, "Flight policy must not be null");
    }
}
