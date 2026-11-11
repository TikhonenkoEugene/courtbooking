package com.courtbook.booking.event;

public record PaymentCompletedEvent(Long bookingId, boolean success) {
}
