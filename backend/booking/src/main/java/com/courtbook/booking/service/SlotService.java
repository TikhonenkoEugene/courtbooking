package com.courtbook.booking.service;

import com.courtbook.booking.client.CatalogClient;
import com.courtbook.booking.client.Court;
import com.courtbook.booking.dto.CourtSlots;
import com.courtbook.booking.dto.Slot;
import com.courtbook.booking.entity.Booking;
import com.courtbook.booking.entity.BookingStatus;
import com.courtbook.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final CatalogClient catalogClient;
    private final BookingRepository bookingRepository;

    public List<CourtSlots> findFreeSlots(String sport, String city, LocalDate date, LocalTime from, LocalTime to) {
        List<Court> courts = catalogClient.searchCourts(sport, city);
        if (courts.isEmpty()) {
            return List.of();
        }

        List<Booking> bookings = bookingRepository.findByCourtIdInAndStatusInAndStartTimeBeforeAndEndTimeAfter(
                courts.stream().map(Court::id).toList(),
                List.of(BookingStatus.PENDING_PAYMENT, BookingStatus.ACTIVE),
                date.plusDays(1).atStartOfDay(),
                date.atStartOfDay());

        List<CourtSlots> result = new ArrayList<>();
        for (Court court : courts) {
            List<Slot> slots = freeSlots(court, bookings, date, from, to);
            if (!slots.isEmpty()) {
                result.add(new CourtSlots(court.id(), court.name(), court.sport(), court.pricePerHour(),
                        court.facilityName(), court.city(), court.address(), slots));
            }
        }
        return result;
    }

    private List<Slot> freeSlots(Court court, List<Booking> bookings, LocalDate date, LocalTime from, LocalTime to) {
        Court.Schedule schedule = court.schedule().stream()
                .filter(s -> s.dayOfWeek() == date.getDayOfWeek().getValue())
                .findFirst()
                .orElse(null);
        if (schedule == null) {
            return List.of();
        }

        List<Slot> slots = new ArrayList<>();
        LocalDateTime start = date.atTime(schedule.openTime());
        LocalDateTime close = date.atTime(schedule.closeTime());
        while (!start.plusHours(1).isAfter(close)) {
            LocalDateTime end = start.plusHours(1);
            boolean inRange = !start.toLocalTime().isBefore(from) && !end.toLocalTime().isAfter(to);
            if (inRange && isFree(court.id(), bookings, start, end)) {
                slots.add(new Slot(start, end));
            }
            start = end;
        }
        return slots;
    }

    private boolean isFree(Long courtId, List<Booking> bookings, LocalDateTime start, LocalDateTime end) {
        return bookings.stream().noneMatch(b -> b.getCourtId().equals(courtId)
                && b.getStartTime().isBefore(end)
                && b.getEndTime().isAfter(start));
    }
}
