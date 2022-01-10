package ru.tinkoff.piapi.robot.services.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TradingStatusChangedEvent {
    private String figi;
    private String tradingStatus;
}
