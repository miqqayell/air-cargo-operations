package be.kdg.prog6.loading.adapter.out;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.loading.domain.Load;
import be.kdg.prog6.loading.domain.Position;
import be.kdg.prog6.loading.domain.Uld;
import be.kdg.prog6.loading.domain.UldCode;
import be.kdg.prog6.loading.domain.Weight;
import be.kdg.prog6.loading.port.out.LoadLoadPort;
import be.kdg.prog6.loading.port.out.LoadRemovePort;
import be.kdg.prog6.loading.port.out.LoadUpdatePort;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(1)
public class LoadPersistenceAdapter implements LoadLoadPort, LoadUpdatePort, LoadRemovePort {

    private final LoadJpaRepository repository;

    LoadPersistenceAdapter(LoadJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Load> loadLoad(FlightId flightId) {
        return repository.findById(LoadJpaId.from(flightId)).map(this::toDomain);
    }

    @Override
    public void updateLoad(Load load) {
        repository.save(toEntity(load));
    }

    @Override
    public void removeLoad(FlightId flightId) {
        repository.deleteById(LoadJpaId.from(flightId));
    }

    private LoadJpaEntity toEntity(Load load) {
        return new LoadJpaEntity(
                LoadJpaId.from(load.getFlightId()),
                load.getDestination().code(),
                load.getScheduledDeparture(),
                load.getEstimatedDeparture(),
                load.getStatus(),
                load.isDeparted(),
                load.getUlds().stream()
                        .map(uld -> new UldJpaEmbeddable(uld.getCode().value(), uld.getWeight().value(),
                                uld.getWeight().unit(), uld.getDestination().code(),
                                uld.isPlaced() ? uld.getPosition().code() : null))
                        .toList());
    }

    private Load toDomain(LoadJpaEntity entity) {
        return Load.restore(
                entity.getId().toDomain(),
                new AirportCode(entity.getDestination()),
                entity.getScheduledDeparture(),
                entity.getEstimatedDeparture(),
                entity.getStatus(),
                entity.isDeparted(),
                entity.getUlds().stream()
                        .map(uld -> Uld.restore(new UldCode(uld.getUldCode()),
                                new Weight(uld.getWeight(), uld.getWeightUnit()),
                                new AirportCode(uld.getDestination()),
                                uld.getPosition() == null ? null : new Position(uld.getPosition())))
                        .toList());
    }
}
