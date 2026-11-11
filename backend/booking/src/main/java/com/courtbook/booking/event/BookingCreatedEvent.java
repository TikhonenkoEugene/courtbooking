package com.courtbook.booking.event;

import java.math.BigDecimal;

public record BookingCreatedEvent(Long bookingId, Long userId, BigDecimal amount) {
}
