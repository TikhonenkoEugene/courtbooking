package com.courtbook.catalog.controller;

import com.courtbook.catalog.dto.CourtRequest;
import com.courtbook.catalog.dto.CourtResponse;
import com.courtbook.catalog.dto.FacilityRequest;
import com.courtbook.catalog.entity.Facility;
import com.courtbook.catalog.service.CourtService;
import com.courtbook.catalog.service.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/owner")
@RequiredArgsConstructor
public class OwnerController {

    private static final String USER_ID = "X-User-Id";

    private final FacilityService facilityService;
    private final CourtService courtService;

    @GetMapping("/facilities")
    public List<Facility> myFacilities(@RequestHeader(USER_ID) Long ownerId) {
        return facilityService.findByOwner(ownerId);
    }

    @GetMapping("/facilities/{id}")
    public Facility getFacility(@RequestHeader(USER_ID) Long ownerId,
                                @PathVariable Long id) {
        return facilityService.getOwnFacility(ownerId, id);
    }

    @PostMapping("/facilities")
    @ResponseStatus(HttpStatus.CREATED)
    public Facility createFacility(@RequestHeader(USER_ID) Long ownerId,
                                   @RequestBody FacilityRequest request) {
        return facilityService.create(ownerId, request);
    }

    @PutMapping("/facilities/{id}")
    public Facility updateFacility(@RequestHeader(USER_ID) Long ownerId,
                                   @PathVariable Long id, @RequestBody FacilityRequest request) {
        return facilityService.update(ownerId, id, request);
    }

    @DeleteMapping("/facilities/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFacility(@RequestHeader(USER_ID) Long ownerId,
                               @PathVariable Long id) {
        facilityService.delete(ownerId, id);
    }

    @GetMapping("/facilities/{facilityId}/courts")
    public List<CourtResponse> facilityCourts(@RequestHeader(USER_ID) Long ownerId,
                                              @PathVariable Long facilityId) {
        return courtService.findByFacility(ownerId, facilityId);
    }

    @PostMapping("/facilities/{facilityId}/courts")
    @ResponseStatus(HttpStatus.CREATED)
    public CourtResponse createCourt(@RequestHeader(USER_ID) Long ownerId,
                                     @PathVariable Long facilityId, @RequestBody CourtRequest request) {
        return courtService.create(ownerId, facilityId, request);
    }

    @PutMapping("/courts/{id}")
    public CourtResponse updateCourt(@RequestHeader(USER_ID) Long ownerId,
                                     @PathVariable Long id, @RequestBody CourtRequest request) {
        return courtService.update(ownerId, id, request);
    }

    @DeleteMapping("/courts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourt(@RequestHeader(USER_ID) Long ownerId,
                            @PathVariable Long id) {
        courtService.delete(ownerId, id);
    }
}
