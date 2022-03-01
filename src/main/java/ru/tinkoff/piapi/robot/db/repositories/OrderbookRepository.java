package ru.tinkoff.piapi.robot.db.repositories;

import ru.tinkoff.piapi.contract.v1.OrderBook;
import ru.tinkoff.piapi.robot.db.repositories.impl.OrderbookRepositoryImpl;

import java.util.List;

public interface OrderbookRepository {

    void addOrderbook(OrderBook orderBook);

    java.sql.Timestamp lastOrderbook(String instrumentType);

    List<OrderbookRepositoryImpl.OrderboookResponse> failedOrderbook();
}
