package com.sami.app.ambulance;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {

    boolean existsByPlateIgnoreCase(String plate);

    boolean existsByPlateIgnoreCaseAndIdNot(String plate, Long id);

    @EntityGraph(attributePaths = { "driver", "emt" })
    Optional<Ambulance> findWithCrewById(Long id);

    @EntityGraph(attributePaths = { "driver", "emt" })
    Page<Ambulance> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = { "driver", "emt" })
    Page<Ambulance> findByStatus(AmbulanceStatus status, Pageable pageable);

    @EntityGraph(attributePaths = { "driver", "emt" })
    @Query("select a from Ambulance a where a.driver.id = :userId or a.emt.id = :userId")
    Optional<Ambulance> findByCrewMember(@Param("userId") Long userId);
}
