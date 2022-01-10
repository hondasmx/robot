package ru.tinkoff.piapi.robot.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.MarketDataResponse;
import ru.tinkoff.piapi.contract.v1.SubscriptionStatus;
import ru.tinkoff.piapi.robot.grpc.StreamConfiguration;
import ru.tinkoff.piapi.robot.db.repositories.CandlesRepository;

@Component
@Slf4j
@RequiredArgsConstructor
public class CandlesProcessor implements MarketdataStreamProcessor {

    private final CandlesRepository candlesRepository;
    private final StreamConfiguration streamConfiguration;


    @Override
    public void process(MarketDataResponse response) {
        if (response.hasSubscribeCandlesResponse()) {
            var count = response.getSubscribeCandlesResponse().getCandlesSubscriptionsList().stream().filter(el -> el.getSubscriptionStatus().equals(SubscriptionStatus.SUBSCRIPTION_STATUS_SUCCESS)).count();
            log.info("success candles subscriptions: {}", count);
        }
        if (!streamConfiguration.getCandles().isDbWriteEnabled()) return;

        if (response.hasCandle()) {
            var candle = response.getCandle();
            var figi = candle.getFigi();
            var time = candle.getTime();
            candlesRepository.addCandle(figi, time);
        }
    }
}
