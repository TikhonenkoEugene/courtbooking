package com.courtbook.booking.dto;

import java.math.BigDecimal;
import java.util.List;

public record CourtSlots(Long courtId, String courtName, String sport, BigDecimal pricePerHour,
                         String facilityName, String city, String address, List<Slot> slots) {
}
