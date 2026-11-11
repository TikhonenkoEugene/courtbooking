package com.courtbook.booking.controller;

import com.courtbook.booking.dto.CreateBookingRequest;
import com.courtbook.booking.entity.Booking;
import com.courtbook.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String USER_ID = "X-User-Id";

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking create(@RequestHeader(USER_ID) Long userId,
                          @RequestBody CreateBookingRequest request) {
        return bookingService.create(userId, request);
    }

    @GetMapping
    public List<Booking> myBookings(@RequestHeader(USER_ID) Long userId) {
        return bookingService.findByUser(userId);
    }

    @PostMapping("/{id}/cancel")
    public Booking cancel(@RequestHeader(USER_ID) Long userId,
                          @PathVariable Long id) {
        return bookingService.cancel(userId, id);
    }
}
