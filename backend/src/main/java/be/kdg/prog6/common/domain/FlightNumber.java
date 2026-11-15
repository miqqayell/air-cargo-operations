package be.kdg.prog6.common.domain;

public record FlightNumber(String value) {

    public FlightNumber {
        if (value == null || !value.matches("[A-Z0-9]{2}[0-9]{1,4}")) {
            throw new IllegalArgumentException("Invalid flight number: " + value);
        }
    }
}
