package com.courtbook.catalog.service;

import com.courtbook.catalog.dto.FacilityRequest;
import com.courtbook.catalog.entity.Facility;
import com.courtbook.catalog.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityRepository facilityRepository;

    public List<Facility> findByOwner(Long ownerId) {
        return facilityRepository.findByOwnerIdOrderById(ownerId);
    }

    public Facility getOwnFacility(Long ownerId, Long id) {
        return facilityRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facility not found"));
    }

    @Transactional
    public Facility create(Long ownerId, FacilityRequest request) {
        Facility facility = new Facility();
        facility.setOwnerId(ownerId);
        fill(facility, request);
        return facilityRepository.save(facility);
    }

    @Transactional
    public Facility update(Long ownerId, Long id, FacilityRequest request) {
        Facility facility = getOwnFacility(ownerId, id);
        fill(facility, request);
        return facility;
    }

    @Transactional
    public void delete(Long ownerId, Long id) {
        facilityRepository.delete(getOwnFacility(ownerId, id));
    }

    private void fill(Facility facility, FacilityRequest request) {
        if (isBlank(request.name()) || isBlank(request.city()) || isBlank(request.address())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name, city and address are required");
        }
        facility.setName(request.name());
        facility.setCity(request.city());
        facility.setAddress(request.address());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
