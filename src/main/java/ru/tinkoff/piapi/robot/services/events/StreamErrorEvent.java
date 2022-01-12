package ru.tinkoff.piapi.robot.services.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class StreamErrorEvent {
    private final String streamName;
}
