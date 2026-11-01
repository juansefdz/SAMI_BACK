package com.sami.app.emergency;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergencyRepository extends JpaRepository<Emergency, Long> {

    @EntityGraph(attributePaths = { "client", "ambulance", "ambulance.driver", "ambulance.emt", "hospital" })
    Optional<Emergency> findDetailedById(Long id);

    @EntityGraph(attributePaths = { "client", "ambulance", "hospital" })
    Page<Emergency> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = { "client", "ambulance", "hospital" })
    Page<Emergency> findByStatus(EmergencyStatus status, Pageable pageable);

    @EntityGraph(attributePaths = { "client", "ambulance", "hospital" })
    Page<Emergency> findByClient_Id(Long clientId, Pageable pageable);

    @EntityGraph(attributePaths = { "client", "ambulance", "ambulance.driver", "ambulance.emt", "hospital" })
    Optional<Emergency> findFirstByAmbulance_IdAndStatusInOrderByCreatedAtDesc(Long ambulanceId,
            Collection<EmergencyStatus> statuses);

    boolean existsByClient_IdAndStatusIn(Long clientId, Collection<EmergencyStatus> statuses);
}
