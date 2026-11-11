package com.courtbook.payment.event;

import com.courtbook.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingCancelledListener {

    private final PaymentService paymentService;

    @KafkaListener(topics = "booking-cancelled",
            properties = "spring.json.value.default.type=com.courtbook.payment.event.BookingCancelledEvent")
    public void handle(BookingCancelledEvent event) {
        paymentService.cancelPayment(event.bookingId());
    }
}
