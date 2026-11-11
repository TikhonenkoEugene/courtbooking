package com.courtbook.catalog.repository;

import com.courtbook.catalog.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacilityRepository extends JpaRepository<Facility, Long> {

    List<Facility> findByOwnerIdOrderById(Long ownerId);

    Optional<Facility> findByIdAndOwnerId(Long id, Long ownerId);
}
