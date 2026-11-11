package com.courtbook.booking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(@Value("${catalog.url}") String catalogUrl) {
        this.restClient = RestClient.create(catalogUrl);
    }

    public Court getCourt(Long id) {
        try {
            return restClient.get()
                    .uri("/api/v1/courts/{id}", id)
                    .retrieve()
                    .body(Court.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Court not found");
        }
    }

    public List<Court> searchCourts(String sport, String city) {
        return restClient.get()
                .uri(uri -> uri.path("/api/v1/courts")
                        .queryParamIfPresent("sport", Optional.ofNullable(sport))
                        .queryParamIfPresent("city", Optional.ofNullable(city))
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
