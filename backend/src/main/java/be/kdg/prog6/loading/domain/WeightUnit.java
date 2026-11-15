package be.kdg.prog6.loading.domain;

import java.math.BigDecimal;

public enum WeightUnit {
    KG(BigDecimal.ONE),
    LB(new BigDecimal("0.45359237"));

    private final BigDecimal kilograms;

    WeightUnit(BigDecimal kilograms) {
        this.kilograms = kilograms;
    }

    BigDecimal kilograms() {
        return kilograms;
    }
}
