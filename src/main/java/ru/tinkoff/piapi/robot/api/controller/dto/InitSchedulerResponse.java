package ru.tinkoff.piapi.robot.api.controller.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class InitSchedulerResponse {
    private final String status;

    public static InitSchedulerResponse of(String status) {
        return new InitSchedulerResponse(status);
    }
}
