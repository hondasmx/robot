package ru.tinkoff.piapi.robot.db.repositories;

import com.google.protobuf.Timestamp;

import java.math.BigDecimal;

public interface TradeRepository {

    void addTrade(String figi, Timestamp timestamp, String direction, long lot, BigDecimal price);

    java.sql.Timestamp lastTrade(String instrumentType);
}
