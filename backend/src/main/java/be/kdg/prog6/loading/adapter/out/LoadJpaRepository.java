package be.kdg.prog6.loading.adapter.out;

import org.springframework.data.jpa.repository.JpaRepository;

interface LoadJpaRepository extends JpaRepository<LoadJpaEntity, LoadJpaId> {
}
