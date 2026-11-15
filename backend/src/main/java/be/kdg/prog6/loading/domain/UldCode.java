package be.kdg.prog6.loading.domain;

public record UldCode(String value) {

    public UldCode {
        if (value == null || !value.matches("[A-Z]{3}[0-9]{4,5}[A-Z0-9]{2}")) {
            throw new IllegalArgumentException("Invalid ULD code: " + value);
        }
    }
}
