package com.courtbook.catalog.service;

import com.courtbook.catalog.dto.CourtRequest;
import com.courtbook.catalog.dto.CourtResponse;
import com.courtbook.catalog.dto.ScheduleDto;
import com.courtbook.catalog.entity.Court;
import com.courtbook.catalog.entity.CourtSchedule;
import com.courtbook.catalog.entity.Facility;
import com.courtbook.catalog.entity.Sport;
import com.courtbook.catalog.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CourtService {

    private final CourtRepository courtRepository;
    private final FacilityService facilityService;

    @Transactional(readOnly = true)
    public List<CourtResponse> search(Sport sport, String city) {
        return courtRepository.search(sport, city).stream().map(CourtResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CourtResponse get(Long id) {
        return courtRepository.findById(id)
                .map(CourtResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Court not found"));
    }

    @Transactional(readOnly = true)
    public List<CourtResponse> findByFacility(Long ownerId, Long facilityId) {
        facilityService.getOwnFacility(ownerId, facilityId);
        return courtRepository.findByFacilityIdOrderById(facilityId).stream().map(CourtResponse::from).toList();
    }

    @Transactional
    public CourtResponse create(Long ownerId, Long facilityId, CourtRequest request) {
        Facility facility = facilityService.getOwnFacility(ownerId, facilityId);
        Court court = new Court();
        court.setFacility(facility);
        fill(court, request);
        return CourtResponse.from(courtRepository.save(court));
    }

    @Transactional
    public CourtResponse update(Long ownerId, Long id, CourtRequest request) {
        Court court = getOwnCourt(ownerId, id);
        fill(court, request);
        return CourtResponse.from(court);
    }

    @Transactional
    public void delete(Long ownerId, Long id) {
        courtRepository.delete(getOwnCourt(ownerId, id));
    }

    private Court getOwnCourt(Long ownerId, Long id) {
        return courtRepository.findByIdAndFacilityOwnerId(id, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Court not found"));
    }

    private void fill(Court court, CourtRequest request) {
        if (request.name() == null || request.name().isBlank() || request.sport() == null
                || request.pricePerHour() == null || request.pricePerHour().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name, sport and pricePerHour are required");
        }
        court.setName(request.name());
        court.setSport(request.sport());
        court.setPricePerHour(request.pricePerHour());

        List<ScheduleDto> schedule = request.schedule() == null ? List.of() : request.schedule();
        Set<Integer> days = new HashSet<>();
        for (ScheduleDto day : schedule) {
            if (day.dayOfWeek() < 1 || day.dayOfWeek() > 7 || !days.add(day.dayOfWeek())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong day of week: " + day.dayOfWeek());
            }
            if (day.openTime() == null || day.closeTime() == null || !day.openTime().isBefore(day.closeTime())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Open time must be before close time");
            }
        }

        court.getSchedule().clear();
        courtRepository.flush();
        for (ScheduleDto day : schedule) {
            CourtSchedule item = new CourtSchedule();
            item.setCourt(court);
            item.setDayOfWeek(day.dayOfWeek());
            item.setOpenTime(day.openTime());
            item.setCloseTime(day.closeTime());
            court.getSchedule().add(item);
        }
    }
}
