package com.courtbook.catalog.controller;

import com.courtbook.catalog.dto.CourtResponse;
import com.courtbook.catalog.entity.Sport;
import com.courtbook.catalog.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    @GetMapping
    public List<CourtResponse> search(@RequestParam(required = false) Sport sport,
                                      @RequestParam(required = false) String city) {
        return courtService.search(sport, city);
    }

    @GetMapping("/{id}")
    public CourtResponse get(@PathVariable Long id) {
        return courtService.get(id);
    }
}
