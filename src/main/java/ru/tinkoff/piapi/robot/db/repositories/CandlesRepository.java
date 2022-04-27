package ru.tinkoff.piapi.robot.db.repositories;

import com.google.protobuf.Timestamp;

import java.math.BigDecimal;

public interface CandlesRepository {

    void addCandle(String figi, Timestamp timestamp, BigDecimal high, BigDecimal low, long volume, BigDecimal close, BigDecimal open);

    java.sql.Timestamp lastCandle();

    java.sql.Timestamp lastCandle(String instrumentType);
}
