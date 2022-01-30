package ru.tinkoff.piapi.robot.processor.marketdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.MarketDataResponse;
import ru.tinkoff.piapi.contract.v1.SubscriptionStatus;
import ru.tinkoff.piapi.robot.db.repositories.TradingStatusRepository;
import ru.tinkoff.piapi.robot.services.events.TradingStatusChangedEvent;


import static ru.tinkoff.piapi.robot.processor.StreamNames.INFO;

@Component
@RequiredArgsConstructor
@Slf4j
public class InfoProcessor implements MarketdataStreamProcessor {

    private final TradingStatusRepository tradingStatusRepository;
    private final ApplicationEventPublisher publisher;

    @Override
    public void process(MarketDataResponse response) {
        if (response.hasTradingStatus()) {
            var resp = response.getTradingStatus();
            var figi = resp.getFigi();
            var tradingStatus = resp.getTradingStatus().name();
            var tradingStatusUpdatedAt = resp.getTime();
            tradingStatusRepository.addTradingStatus(figi, tradingStatus, tradingStatusUpdatedAt);
            publisher.publishEvent(new TradingStatusChangedEvent(figi, tradingStatus, tradingStatusUpdatedAt));
        }
        else if (response.hasSubscribeInfoResponse()) {
            var count = response.getSubscribeInfoResponse().getInfoSubscriptionsList().stream().filter(el -> el.getSubscriptionStatus().equals(SubscriptionStatus.SUBSCRIPTION_STATUS_SUCCESS)).count();
            log.info("success info subscriptions: {}", count);
        }
    }

    @Override
    public String streamName() {
        return INFO;
    }
}
