package be.kdg.prog6.common.domain;

public record AirportCode(String code) {

    public AirportCode {
        if (code == null || !code.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Invalid airport code: " + code);
        }
    }
}
