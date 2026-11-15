package be.kdg.prog6.flights.adapter.in;

import be.kdg.prog6.flights.domain.FlightPolicy;

public record FlightPolicyDto(long actualTimeToleranceMinutes, long retentionDays) {

    static FlightPolicyDto from(FlightPolicy flightPolicy) {
        return new FlightPolicyDto(flightPolicy.actualTimeTolerance().toMinutes(), flightPolicy.retention().toDays());
    }
}
