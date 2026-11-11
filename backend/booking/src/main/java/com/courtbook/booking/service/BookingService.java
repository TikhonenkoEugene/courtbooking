package com.courtbook.booking.service;

import com.courtbook.booking.client.CatalogClient;
import com.courtbook.booking.client.Court;
import com.courtbook.booking.dto.CreateBookingRequest;
import com.courtbook.booking.entity.Booking;
import com.courtbook.booking.entity.BookingStatus;
import com.courtbook.booking.event.BookingCancelledEvent;
import com.courtbook.booking.event.BookingCreatedEvent;
import com.courtbook.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    public static final String BOOKING_CREATED_TOPIC = "booking-created";
    public static final String BOOKING_CANCELLED_TOPIC = "booking-cancelled";
    public static final int CANCEL_HOURS_BEFORE_START = 2;

    private final BookingRepository bookingRepository;
    private final CatalogClient catalogClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public Booking create(Long userId, CreateBookingRequest request) {
        if (request.courtId() == null || request.startTime() == null || request.hours() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "courtId, startTime and hours are required");
        }
        Court court = catalogClient.getCourt(request.courtId());

        LocalDateTime start = request.startTime();
        LocalDateTime end = start.plusHours(request.hours());
        if (start.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot book time in the past");
        }
        checkSchedule(court, start, end);

        boolean busy = bookingRepository.existsByCourtIdAndStatusInAndStartTimeBeforeAndEndTimeAfter(
                court.id(), List.of(BookingStatus.PENDING_PAYMENT, BookingStatus.ACTIVE), end, start);
        if (busy) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Court is already booked for this time");
        }

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setCourtId(court.id());
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setPrice(court.pricePerHour().multiply(BigDecimal.valueOf(request.hours())));
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        bookingRepository.save(booking);

        kafkaTemplate.send(BOOKING_CREATED_TOPIC, booking.getId().toString(),
                new BookingCreatedEvent(booking.getId(), userId, booking.getPrice()));
        return booking;
    }

    private void checkSchedule(Court court, LocalDateTime start, LocalDateTime end) {
        Court.Schedule day = court.schedule().stream()
                .filter(s -> s.dayOfWeek() == start.getDayOfWeek().getValue())
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Court is closed on this day"));

        boolean sameDay = start.toLocalDate().equals(end.toLocalDate());
        if (!sameDay || start.toLocalTime().isBefore(day.openTime()) || end.toLocalTime().isAfter(day.closeTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Court works from " + day.openTime() + " to " + day.closeTime());
        }
    }

    public List<Booking> findByUser(Long userId) {
        return bookingRepository.findByUserIdOrderByStartTimeDesc(userId);
    }

    @Transactional
    public Booking cancel(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .filter(b -> b.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking is already cancelled");
        }
        if (LocalDateTime.now().plusHours(CANCEL_HOURS_BEFORE_START).isAfter(booking.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Booking can be cancelled no later than " + CANCEL_HOURS_BEFORE_START + " hours before start");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        kafkaTemplate.send(BOOKING_CANCELLED_TOPIC, booking.getId().toString(),
                new BookingCancelledEvent(booking.getId()));
        return booking;
    }

    @Transactional
    public void onPaymentCompleted(Long bookingId, boolean success) {
        bookingRepository.findById(bookingId)
                .filter(booking -> booking.getStatus() == BookingStatus.PENDING_PAYMENT)
                .ifPresent(booking -> booking.setStatus(success ? BookingStatus.ACTIVE : BookingStatus.CANCELLED));
    }
}
