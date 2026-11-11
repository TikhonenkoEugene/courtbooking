package com.courtbook.catalog.dto;

import com.courtbook.catalog.entity.Court;
import com.courtbook.catalog.entity.Sport;

import java.math.BigDecimal;
import java.util.List;

public record CourtResponse(Long id, String name, Sport sport, BigDecimal pricePerHour,
                            Long facilityId, String facilityName, String city, String address,
                            List<ScheduleDto> schedule) {

    public static CourtResponse from(Court court) {
        return new CourtResponse(
                court.getId(),
                court.getName(),
                court.getSport(),
                court.getPricePerHour(),
                court.getFacility().getId(),
                court.getFacility().getName(),
                court.getFacility().getCity(),
                court.getFacility().getAddress(),
                court.getSchedule().stream()
                        .map(s -> new ScheduleDto(s.getDayOfWeek(), s.getOpenTime(), s.getCloseTime()))
                        .toList());
    }
}
