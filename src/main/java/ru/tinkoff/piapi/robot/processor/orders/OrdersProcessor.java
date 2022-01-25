package ru.tinkoff.piapi.robot.processor.orders;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.TradesStreamResponse;

import java.util.List;

import static ru.tinkoff.piapi.robot.processor.StreamNames.ORDERS;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrdersProcessor {


    public void process(TradesStreamResponse response, List<Timestamp> pings) {
        if (response.hasPing()) {
            pings.add(response.getPing().getTime());
        } else {
            log.info("body {}", response.getOrderTrades());
        }
    }

    public String streamName() {
        return ORDERS;
    }
}
