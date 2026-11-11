package com.courtbook.booking.dto;

import java.time.LocalDateTime;

public record CreateBookingRequest(Long courtId, LocalDateTime startTime, int hours) {
}
