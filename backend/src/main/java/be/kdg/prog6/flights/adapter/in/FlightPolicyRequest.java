package be.kdg.prog6.flights.adapter.in;

import be.kdg.prog6.flights.domain.FlightPolicy;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;

public record FlightPolicyRequest(@NotNull Integer actualTimeToleranceMinutes, @NotNull Integer retentionDays) {

    FlightPolicy toFlightPolicy() {
        return new FlightPolicy(Duration.ofMinutes(actualTimeToleranceMinutes), Duration.ofDays(retentionDays));
    }
}
