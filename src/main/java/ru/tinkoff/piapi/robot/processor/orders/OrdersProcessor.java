package ru.tinkoff.piapi.robot.processor.orders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.MarketDataResponse;
import ru.tinkoff.piapi.contract.v1.TradesStreamResponse;
import ru.tinkoff.piapi.robot.processor.StreamProcessor;

import static ru.tinkoff.piapi.robot.processor.StreamNames.ORDERS;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrdersProcessor implements StreamProcessor<TradesStreamResponse> {


    public void process(TradesStreamResponse response) {
        if (!response.hasPing()) {
            log.info("{}", response.getOrderTrades());
        }
    }

    public String streamName() {
        return ORDERS;
    }
}
