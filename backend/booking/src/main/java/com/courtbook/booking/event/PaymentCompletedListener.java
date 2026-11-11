package com.courtbook.booking.event;

import com.courtbook.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCompletedListener {

    private final BookingService bookingService;

    @KafkaListener(topics = "payment-completed")
    public void handle(PaymentCompletedEvent event) {
        bookingService.onPaymentCompleted(event.bookingId(), event.success());
    }
}
