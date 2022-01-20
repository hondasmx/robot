package ru.tinkoff.piapi.robot.processor.orders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.TradesStreamResponse;

import static ru.tinkoff.piapi.robot.processor.StreamNames.ORDERS;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrdersProcessor {


    public void process(TradesStreamResponse response) {
        log.info("ping {}", response.getPing());
        log.info("body {}", response.getOrderTrades().getTradesList());
    }

    public String streamName() {
        return ORDERS;
    }
}
