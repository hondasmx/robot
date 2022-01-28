package ru.tinkoff.piapi.robot.db.repositories;

import com.google.protobuf.Timestamp;

import java.util.List;

public interface TradingStatusRepository {
    void addTradingStatus(String figi, String tradingStatus, Timestamp tradingStatusUpdatedAt);

    List<String> figiByTradingStatus(String tradingStatus);
}
