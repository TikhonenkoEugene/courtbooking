package com.courtbook.booking.client;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record Court(Long id, String name, String sport, BigDecimal pricePerHour,
                    String facilityName, String city, String address, List<Schedule> schedule) {

    public record Schedule(int dayOfWeek, LocalTime openTime, LocalTime closeTime) {
    }
}
