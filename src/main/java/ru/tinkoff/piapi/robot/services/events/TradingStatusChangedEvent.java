package ru.tinkoff.piapi.robot.services.events;

import com.google.protobuf.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TradingStatusChangedEvent {
    private String figi;
    private String tradingStatus;
    private Timestamp tradingStatusUpdatedAt;
}
