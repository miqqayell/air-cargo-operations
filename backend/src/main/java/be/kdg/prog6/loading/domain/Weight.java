package be.kdg.prog6.loading.domain;

import java.math.BigDecimal;

public record Weight(double value, WeightUnit unit) {

    public Weight {
        if (value <= 0) {
            throw new IllegalArgumentException("A weight must be positive");
        }
        if (unit == null) {
            throw new IllegalArgumentException("A weight needs a unit");
        }
    }

    public BigDecimal inKilograms() {
        return BigDecimal.valueOf(value).multiply(unit.kilograms());
    }
}
