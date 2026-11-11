package com.courtbook.payment.event;

import com.courtbook.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingCreatedListener {

    private final PaymentService paymentService;

    @KafkaListener(topics = "booking-created")
    public void handle(BookingCreatedEvent event) {
        paymentService.createPayment(event);
    }
}
