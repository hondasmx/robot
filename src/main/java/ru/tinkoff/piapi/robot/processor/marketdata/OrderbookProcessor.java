package ru.tinkoff.piapi.robot.processor.marketdata;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.MarketDataResponse;
import ru.tinkoff.piapi.contract.v1.SubscriptionStatus;
import ru.tinkoff.piapi.robot.db.repositories.OrderbookRepository;

import java.util.List;

import static ru.tinkoff.piapi.robot.processor.StreamNames.ORDERBOOK;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderbookProcessor implements MarketdataStreamProcessor {

    private final OrderbookRepository orderbookRepository;

    @Override
    public void process(MarketDataResponse response, List<Timestamp> pings) {
        if (response.hasOrderbook()) {
            var orderbook = response.getOrderbook();
            var figi = orderbook.getFigi();
            var time = orderbook.getTime();
            orderbookRepository.addOrderbook(figi, time);
        } else if (response.hasPing()) {
            pings.add(response.getPing().getTime());
        } else if (response.hasSubscribeOrderBookResponse()) {
            var count = response.getSubscribeOrderBookResponse().getOrderBookSubscriptionsList().stream().filter(el -> el.getSubscriptionStatus().equals(SubscriptionStatus.SUBSCRIPTION_STATUS_SUCCESS)).count();
            log.info("success orderbook subscriptions: {}", count);
        }
    }

    @Override
    public String streamName() {
        return ORDERBOOK;
    }
}
