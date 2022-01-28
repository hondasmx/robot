package ru.tinkoff.piapi.robot.db.repositories;

import com.google.protobuf.Timestamp;

public interface TradeRepository {

    void addTrade(String figi, Timestamp timestamp);

    java.sql.Timestamp lastTrade(String instrumentType);
}
