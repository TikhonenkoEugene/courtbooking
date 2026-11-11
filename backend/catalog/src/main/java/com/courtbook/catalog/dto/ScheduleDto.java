package com.courtbook.catalog.dto;

import java.time.LocalTime;

public record ScheduleDto(int dayOfWeek, LocalTime openTime, LocalTime closeTime) {
}
