package com.courtbook.booking.dto;

import java.time.LocalDateTime;

public record Slot(LocalDateTime start, LocalDateTime end) {
}
