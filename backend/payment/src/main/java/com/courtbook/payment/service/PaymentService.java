package com.courtbook.payment.service;

import com.courtbook.payment.entity.Payment;
import com.courtbook.payment.entity.PaymentStatus;
import com.courtbook.payment.entity.Refund;
import com.courtbook.payment.entity.RefundStatus;
import com.courtbook.payment.event.BookingCreatedEvent;
import com.courtbook.payment.event.PaymentCompletedEvent;
import com.courtbook.payment.repository.PaymentRepository;
import com.courtbook.payment.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PaymentService {

    public static final String PAYMENT_COMPLETED_TOPIC = "payment-completed";

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void createPayment(BookingCreatedEvent event) {
        if (paymentRepository.findByBookingId(event.bookingId()).isPresent()) {
            return;
        }
        Payment payment = new Payment();
        payment.setBookingId(event.bookingId());
        payment.setUserId(event.userId());
        payment.setAmount(event.amount());
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);
    }

    @Transactional
    public Payment pay(Long userId, Long bookingId, boolean success) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .filter(p -> p.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment is already processed");
        }
        payment.setStatus(success ? PaymentStatus.PAID : PaymentStatus.FAILED);

        kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, bookingId.toString(), new PaymentCompletedEvent(bookingId, success));
        return payment;
    }

    @Transactional
    public void cancelPayment(Long bookingId) {
        paymentRepository.findByBookingId(bookingId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.PAID) {
                payment.setStatus(PaymentStatus.REFUNDED);
                createRefund(payment);
            } else if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.CANCELLED);
            }
        });
    }

    private void createRefund(Payment payment) {
        Refund refund = new Refund();
        refund.setPaymentId(payment.getId());
        refund.setBookingId(payment.getBookingId());
        refund.setUserId(payment.getUserId());
        refund.setAmount(payment.getAmount());
        refund.setStatus(RefundStatus.COMPLETED);
        refundRepository.save(refund);
    }
}
