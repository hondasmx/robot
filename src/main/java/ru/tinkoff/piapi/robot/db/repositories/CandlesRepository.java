package ru.tinkoff.piapi.robot.db.repositories;

import com.google.protobuf.Timestamp;

public interface CandlesRepository {

    void addCandle(String figi, Timestamp timestamp);

    java.sql.Timestamp lastCandle();

    java.sql.Timestamp lastCandle(String instrumentType);
}
