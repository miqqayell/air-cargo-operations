package be.kdg.prog6.loading.adapter.out;

import be.kdg.prog6.loading.domain.CheckKind;
import be.kdg.prog6.loading.domain.LoadingPolicy;
import be.kdg.prog6.loading.port.out.LoadingPolicyPort;
import be.kdg.prog6.loading.port.out.UpdateLoadingPolicyPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

@Component
public class LoadingPolicyPersistenceAdapter implements LoadingPolicyPort, UpdateLoadingPolicyPort {

    private final LoadingPolicyJpaRepository repository;
    private final LoadingPolicy defaultPolicy;

    LoadingPolicyPersistenceAdapter(
            LoadingPolicyJpaRepository repository,
            @Value("${loading.policy.window-opens-before-std}") Duration windowOpensBeforeStd,
            @Value("${loading.policy.window-closes-before-etd}") Duration windowClosesBeforeEtd,
            @Value("${loading.policy.max-lateral-difference-percent}") BigDecimal maxLateralDifferencePercent,
            @Value("${loading.policy.offloading-check}") CheckKind offloadingCheck) {
        this.repository = repository;
        this.defaultPolicy = new LoadingPolicy(windowOpensBeforeStd, windowClosesBeforeEtd,
                maxLateralDifferencePercent, offloadingCheck);
    }

    @Override
    public LoadingPolicy loadingPolicy() {
        return repository.findById(LoadingPolicyJpaEntity.SINGLE_ROW)
                .map(entity -> new LoadingPolicy(
                        Duration.ofHours(entity.getWindowOpensHoursBeforeStd()),
                        Duration.ofMinutes(entity.getWindowClosesMinutesBeforeEtd()),
                        entity.getMaxLateralDifferencePercent().stripTrailingZeros(),
                        entity.getOffloadingCheck()))
                .orElse(defaultPolicy);
    }

    @Override
    public void updateLoadingPolicy(LoadingPolicy loadingPolicy) {
        repository.save(new LoadingPolicyJpaEntity(
                (int) loadingPolicy.windowOpensBeforeScheduledDeparture().toHours(),
                (int) loadingPolicy.windowClosesBeforeEstimatedDeparture().toMinutes(),
                loadingPolicy.maxLateralDifferencePercent(),
                loadingPolicy.offloadingCheck()));
    }
}
