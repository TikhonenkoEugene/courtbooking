package com.courtbook.catalog.dto;

import com.courtbook.catalog.entity.Sport;

import java.math.BigDecimal;
import java.util.List;

public record CourtRequest(String name, Sport sport, BigDecimal pricePerHour, List<ScheduleDto> schedule) {
}
