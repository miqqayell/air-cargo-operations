package be.kdg.prog6.flights.adapter.out;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.flights.domain.AircraftRegistration;
import be.kdg.prog6.flights.domain.Delay;
import be.kdg.prog6.flights.domain.DelayId;
import be.kdg.prog6.flights.domain.Flight;
import be.kdg.prog6.flights.domain.FlightSearchCriteria;
import be.kdg.prog6.flights.port.out.FlightLoadPort;
import be.kdg.prog6.flights.port.out.FlightRemovePort;
import be.kdg.prog6.flights.port.out.FlightSearchPort;
import be.kdg.prog6.flights.port.out.FlightUpdatePort;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@Order(1)
public class FlightPersistenceAdapter implements FlightLoadPort, FlightUpdatePort, FlightRemovePort, FlightSearchPort {

    private static final Sort BY_SCHEDULED_DEPARTURE = Sort.by("scheduledDeparture", "id.flightNumber");

    private final FlightJpaRepository repository;

    FlightPersistenceAdapter(FlightJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Flight> loadFlight(FlightId flightId) {
        return repository.findById(FlightJpaId.from(flightId)).map(this::toDomain);
    }

    @Override
    public List<Flight> loadFlightsScheduledBefore(LocalDate date) {
        return repository.findByIdScheduledDepartureDateBefore(date).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Flight> searchFlights(FlightSearchCriteria criteria) {
        return repository.findAll(FlightJpaSpecifications.matching(criteria), BY_SCHEDULED_DEPARTURE).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void updateFlight(Flight flight) {
        repository.save(toEntity(flight));
    }

    @Override
    public void removeFlight(Flight flight) {
        repository.deleteById(FlightJpaId.from(flight.getId()));
    }

    private FlightJpaEntity toEntity(Flight flight) {
        return new FlightJpaEntity(
                FlightJpaId.from(flight.getId()),
                flight.getArrivalAirport().code(),
                flight.getAircraftRegistration().value(),
                flight.getScheduledDeparture(),
                flight.getScheduledArrival(),
                flight.getActualDeparture(),
                flight.getActualArrival(),
                flight.isFinalLoadReceived(),
                flight.getDelays().stream()
                        .map(delay -> new DelayJpaEmbeddable(delay.getId().id(), delay.getSide(),
                                delay.getMinutes(), delay.getDelayCode(), delay.getDescription()))
                        .toList());
    }

    private Flight toDomain(FlightJpaEntity entity) {
        return Flight.restore(
                entity.getId().toDomain(),
                new AirportCode(entity.getArrivalAirport()),
                new AircraftRegistration(entity.getAircraftRegistration()),
                entity.getScheduledDeparture(),
                entity.getScheduledArrival(),
                entity.getDelays().stream()
                        .map(delay -> Delay.restore(new DelayId(delay.getDelayId()), delay.getSide(),
                                delay.getMinutes(), delay.getDelayCode(), delay.getDescription()))
                        .toList(),
                entity.getActualDeparture(),
                entity.getActualArrival(),
                entity.isFinalLoadReceived());
    }
}
