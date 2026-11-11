package com.courtbook.booking.controller;

import com.courtbook.booking.dto.CourtSlots;
import com.courtbook.booking.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    @GetMapping
    public List<CourtSlots> freeSlots(@RequestParam(required = false) String sport,
                                      @RequestParam(required = false) String city,
                                      @RequestParam LocalDate date,
                                      @RequestParam(defaultValue = "00:00") LocalTime from,
                                      @RequestParam(defaultValue = "23:59") LocalTime to) {
        return slotService.findFreeSlots(sport, city, date, from, to);
    }
}
