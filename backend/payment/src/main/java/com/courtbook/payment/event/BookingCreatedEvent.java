package com.courtbook.payment.event;

import java.math.BigDecimal;

public record BookingCreatedEvent(Long bookingId, Long userId, BigDecimal amount) {
}
