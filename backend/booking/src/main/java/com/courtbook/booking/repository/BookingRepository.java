package com.courtbook.booking.repository;

import com.courtbook.booking.entity.Booking;
import com.courtbook.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByStartTimeDesc(Long userId);

    List<Booking> findByCourtIdInAndStatusInAndStartTimeBeforeAndEndTimeAfter(
            Collection<Long> courtIds, Collection<BookingStatus> statuses, LocalDateTime end, LocalDateTime start);

    boolean existsByCourtIdAndStatusInAndStartTimeBeforeAndEndTimeAfter(
            Long courtId, Collection<BookingStatus> statuses, LocalDateTime end, LocalDateTime start);
}
