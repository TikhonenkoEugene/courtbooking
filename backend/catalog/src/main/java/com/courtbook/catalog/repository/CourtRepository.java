package com.courtbook.catalog.repository;

import com.courtbook.catalog.entity.Court;
import com.courtbook.catalog.entity.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourtRepository extends JpaRepository<Court, Long> {

    @Query("""
            select c from Court c join c.facility f
            where (:sport is null or c.sport = :sport)
              and (:city is null or f.city = :city)
            order by c.id
            """)
    List<Court> search(Sport sport, String city);

    List<Court> findByFacilityIdOrderById(Long facilityId);

    Optional<Court> findByIdAndFacilityOwnerId(Long id, Long ownerId);
}
