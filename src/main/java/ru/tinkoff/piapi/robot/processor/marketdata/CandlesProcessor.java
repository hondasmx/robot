package ru.tinkoff.piapi.robot.processor.marketdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.MarketDataResponse;
import ru.tinkoff.piapi.contract.v1.SubscriptionStatus;
import ru.tinkoff.piapi.robot.db.repositories.CandlesRepository;
import ru.tinkoff.piapi.robot.processor.StreamProcessor;
import ru.tinkoff.piapi.robot.utils.MoneyUtils;

import static ru.tinkoff.piapi.robot.processor.StreamNames.CANDLES;
import static ru.tinkoff.piapi.robot.utils.MoneyUtils.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class CandlesProcessor implements StreamProcessor<MarketDataResponse> {

    private final CandlesRepository candlesRepository;

    @Override
    public void process(MarketDataResponse response) {
        if (response.hasCandle()) {
            var candle = response.getCandle();
            var figi = candle.getFigi();
            var time = candle.getTime();
            var high = quotationToBigDecimal(candle.getHigh());
            var low = quotationToBigDecimal(candle.getLow());
            var volume = candle.getVolume();
            var close = quotationToBigDecimal(candle.getClose());
            var open = quotationToBigDecimal(candle.getOpen());
            candlesRepository.addCandle(figi, time, high, low, volume, close, open);
        }  else if (response.hasSubscribeCandlesResponse()) {
            var count = response.getSubscribeCandlesResponse().getCandlesSubscriptionsList().stream().filter(el -> el.getSubscriptionStatus().equals(SubscriptionStatus.SUBSCRIPTION_STATUS_SUCCESS)).count();
            log.info("success candles subscriptions: {}", count);
        }
    }

    @Override
    public String streamName() {
        return CANDLES;
    }
}
