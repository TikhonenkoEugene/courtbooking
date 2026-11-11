package com.courtbook.payment.event;

public record PaymentCompletedEvent(Long bookingId, boolean success) {
}
