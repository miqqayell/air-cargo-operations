package be.kdg.prog6.flights.domain;

public record AircraftRegistration(String value) {

    public AircraftRegistration {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Aircraft registration is required");
        }
    }
}
