package be.kdg.prog6.flights.adapter.out;

import org.springframework.data.jpa.repository.JpaRepository;

interface FlightPolicyJpaRepository extends JpaRepository<FlightPolicyJpaEntity, Integer> {
}
