package ru.tinkoff.piapi.robot.processor.marketdata;

import com.google.protobuf.Timestamp;
import ru.tinkoff.piapi.contract.v1.MarketDataResponse;

import java.util.List;

public interface MarketdataStreamProcessor {
    void process(MarketDataResponse response, List<Timestamp> pings);

    String streamName();
}
