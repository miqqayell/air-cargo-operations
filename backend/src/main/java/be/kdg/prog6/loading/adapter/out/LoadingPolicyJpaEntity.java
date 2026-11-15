package be.kdg.prog6.loading.adapter.out;

import be.kdg.prog6.loading.domain.CheckKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(schema = "kdg_loading", name = "loading_policy")
class LoadingPolicyJpaEntity {

    static final int SINGLE_ROW = 1;

    @Id
    private Integer id;

    @Column(nullable = false)
    private int windowOpensHoursBeforeStd;

    @Column(nullable = false)
    private int windowClosesMinutesBeforeEtd;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal maxLateralDifferencePercent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckKind offloadingCheck;

    protected LoadingPolicyJpaEntity() {
    }

    LoadingPolicyJpaEntity(int windowOpensHoursBeforeStd, int windowClosesMinutesBeforeEtd,
                           BigDecimal maxLateralDifferencePercent, CheckKind offloadingCheck) {
        this.id = SINGLE_ROW;
        this.windowOpensHoursBeforeStd = windowOpensHoursBeforeStd;
        this.windowClosesMinutesBeforeEtd = windowClosesMinutesBeforeEtd;
        this.maxLateralDifferencePercent = maxLateralDifferencePercent;
        this.offloadingCheck = offloadingCheck;
    }

    int getWindowOpensHoursBeforeStd() {
        return windowOpensHoursBeforeStd;
    }

    int getWindowClosesMinutesBeforeEtd() {
        return windowClosesMinutesBeforeEtd;
    }

    BigDecimal getMaxLateralDifferencePercent() {
        return maxLateralDifferencePercent;
    }

    CheckKind getOffloadingCheck() {
        return offloadingCheck;
    }
}
