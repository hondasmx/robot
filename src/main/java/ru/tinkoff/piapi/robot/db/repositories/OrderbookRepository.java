package ru.tinkoff.piapi.robot.db.repositories;

import ru.tinkoff.piapi.contract.v1.OrderBook;
import ru.tinkoff.piapi.robot.db.repositories.impl.OrderbookRepositoryImpl;

import java.util.List;
import java.util.Set;

public interface OrderbookRepository {

    void addOrderbook(OrderBook orderBook);

    java.sql.Timestamp lastOrderbook(String instrumentType);

    List<OrderbookRepositoryImpl.OrderbookResponse> failedOrderbook();

    Set<OrderbookRepositoryImpl.TimeDiffResponse> timeDiffOrderbook();

    Set<OrderbookRepositoryImpl.OrderbookResponse> bidsOutOfLits();

    Set<String> zeroLimits();
}
