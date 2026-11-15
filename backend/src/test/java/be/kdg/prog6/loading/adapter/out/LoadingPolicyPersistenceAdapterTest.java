package be.kdg.prog6.loading.adapter.out;

import be.kdg.prog6.TestcontainersConfiguration;
import be.kdg.prog6.loading.domain.CheckKind;
import be.kdg.prog6.loading.domain.LoadingPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class LoadingPolicyPersistenceAdapterTest {

    @Autowired
    private LoadingPolicyPersistenceAdapter adapter;
    @Autowired
    private LoadingPolicyJpaRepository repository;

    @AfterEach
    void backToTheDefaults() {
        repository.deleteAll();
    }

    @Test
    void withoutSavedSettingsTheDefaultsOfTheConfigurationApply() {
        assertEquals(new LoadingPolicy(Duration.ofHours(12), Duration.ofMinutes(20), BigDecimal.TEN, CheckKind.HARD),
                adapter.loadingPolicy());
    }

    @Test
    void savedSettingsReplaceTheDefaults() {
        var policy = new LoadingPolicy(Duration.ofHours(24), Duration.ofMinutes(45), new BigDecimal("7.5"),
                CheckKind.SOFT);

        adapter.updateLoadingPolicy(policy);

        assertEquals(policy, adapter.loadingPolicy());
        assertEquals(1, repository.count());
    }
}
