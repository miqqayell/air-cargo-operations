package be.kdg.prog6.flights.adapter.out;

import be.kdg.prog6.flights.domain.FlightPolicy;
import be.kdg.prog6.flights.port.out.FlightPolicyPort;
import be.kdg.prog6.flights.port.out.UpdateFlightPolicyPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class FlightPolicyPersistenceAdapter implements FlightPolicyPort, UpdateFlightPolicyPort {

    private final FlightPolicyJpaRepository repository;
    private final FlightPolicy defaultPolicy;

    FlightPolicyPersistenceAdapter(FlightPolicyJpaRepository repository,
                                   @Value("${flights.policy.actual-time-tolerance}") Duration actualTimeTolerance,
                                   @Value("${flights.policy.retention}") Duration retention) {
        this.repository = repository;
        this.defaultPolicy = new FlightPolicy(actualTimeTolerance, retention);
    }

    @Override
    public FlightPolicy flightPolicy() {
        return repository.findById(FlightPolicyJpaEntity.SINGLE_ROW)
                .map(entity -> new FlightPolicy(Duration.ofMinutes(entity.getActualTimeToleranceMinutes()),
                        Duration.ofDays(entity.getRetentionDays())))
                .orElse(defaultPolicy);
    }

    @Override
    public void updateFlightPolicy(FlightPolicy flightPolicy) {
        repository.save(new FlightPolicyJpaEntity((int) flightPolicy.actualTimeTolerance().toMinutes(),
                (int) flightPolicy.retention().toDays()));
    }
}
