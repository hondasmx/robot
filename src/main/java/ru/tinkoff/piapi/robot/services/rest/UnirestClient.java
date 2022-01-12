package ru.tinkoff.piapi.robot.services.rest;

import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UnirestClient {

    public void doGetRequest(String endpoint) {
        Unirest
                .post(endpoint)
                .asEmpty();
    }

    public void doPostRequest(String endpoint, Object body) {
        log.info("POST request. endpoint {}", endpoint);
        Unirest
                .post(endpoint)
                .header("Content-Type", "application/json")
                .body(body)
                .asJson()
                .ifFailure(response -> {
                    log.error("Oh No! Status: " + response.getStatus());
                    response.getParsingError().ifPresent(e -> {
                        log.error("Parsing Exception: ", e);
                        log.error("Original body: " + e.getOriginalBody());
                    });
                });
    }
}
