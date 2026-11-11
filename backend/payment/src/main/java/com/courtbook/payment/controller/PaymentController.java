package com.courtbook.payment.controller;

import com.courtbook.payment.entity.Payment;
import com.courtbook.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{bookingId}/pay")
    public Payment pay(@RequestHeader("X-User-Id") Long userId,
                       @PathVariable Long bookingId, @RequestParam(defaultValue = "true") boolean success) {
        return paymentService.pay(userId, bookingId, success);
    }
}
