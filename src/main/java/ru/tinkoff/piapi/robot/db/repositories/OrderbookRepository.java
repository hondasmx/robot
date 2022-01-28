package ru.tinkoff.piapi.robot.db.repositories;

import com.google.protobuf.Timestamp;

public interface OrderbookRepository {

    void addOrderbook(String figi, Timestamp timestamp);

    java.sql.Timestamp lastOrderbook(String instrumentType);
}
