package ru.tinkoff.piapi.robot.processor;

import ru.tinkoff.piapi.contract.v1.MarketDataResponse;

public interface MarketdataStreamProcessor {
    void process(MarketDataResponse response);
}
