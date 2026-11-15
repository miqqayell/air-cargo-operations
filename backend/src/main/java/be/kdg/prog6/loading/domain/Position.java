package be.kdg.prog6.loading.domain;

public record Position(String code) {

    public Position {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("A position needs a code");
        }
        code = code.trim().toUpperCase();
    }
}
