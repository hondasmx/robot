package ru.tinkoff.piapi.robot.db.repositories;

import com.google.protobuf.Timestamp;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.Quotation;

public interface LastPricesRepository {

    void addLastPrice(LastPrice lastPrice);

    void addLastPrice(String figi, Quotation lastPrice, Timestamp timestamp);
}
