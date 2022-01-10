package ru.tinkoff.piapi.robot.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.MarketDataResponse;
import ru.tinkoff.piapi.contract.v1.SubscriptionStatus;
import ru.tinkoff.piapi.robot.grpc.StreamConfiguration;
import ru.tinkoff.piapi.robot.db.repositories.OrderbookRepository;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderbookProcessor implements MarketdataStreamProcessor {

    private final OrderbookRepository orderbookRepository;
    private final StreamConfiguration streamConfiguration;

    @Override
    public void process(MarketDataResponse response) {
        if (response.hasSubscribeOrderBookResponse()) {
            var count = response.getSubscribeOrderBookResponse().getOrderBookSubscriptionsList().stream().filter(el -> el.getSubscriptionStatus().equals(SubscriptionStatus.SUBSCRIPTION_STATUS_SUCCESS)).count();
            log.info("success orderbook subscriptions: {}", count);
        }
        if (!streamConfiguration.getOrderbook().isDbWriteEnabled()) return;
        if (response.hasOrderbook()) {
            var orderbook = response.getOrderbook();
            var figi = orderbook.getFigi();
            var time = orderbook.getTime();
            orderbookRepository.addOrderbook(figi, time);
        }
    }
}
