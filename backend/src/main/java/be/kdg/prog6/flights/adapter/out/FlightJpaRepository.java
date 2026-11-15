package be.kdg.prog6.flights.adapter.out;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

interface FlightJpaRepository extends JpaRepository<FlightJpaEntity, FlightJpaId>,
        JpaSpecificationExecutor<FlightJpaEntity> {

    List<FlightJpaEntity> findByIdScheduledDepartureDateBefore(LocalDate date);
}
