package ru.tinkoff.piapi.robot.processor.marketdata;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.MarketDataResponse;
import ru.tinkoff.piapi.contract.v1.SubscriptionStatus;
import ru.tinkoff.piapi.robot.db.repositories.TradeRepository;
import ru.tinkoff.piapi.robot.grpc.StreamConfiguration;

import java.util.List;

import static ru.tinkoff.piapi.robot.processor.StreamNames.TRADES;

@Component
@Slf4j
@RequiredArgsConstructor
public class TradesProcessor implements MarketdataStreamProcessor {

    private final TradeRepository repository;
    private final StreamConfiguration streamConfiguration;


    @Override
    public void process(MarketDataResponse response, List<Timestamp> pings) {
        if (response.hasPing()) {
            pings.add(response.getPing().getTime());
        }
        if (response.hasSubscribeTradesResponse()) {
            var count = response.getSubscribeTradesResponse().getTradeSubscriptionsList().stream().filter(el -> el.getSubscriptionStatus().equals(SubscriptionStatus.SUBSCRIPTION_STATUS_SUCCESS)).count();
            log.info("success trades subscriptions: {}", count);
        }
        if (!streamConfiguration.getTrades().isDbWriteEnabled()) return;

        if (response.hasTrade()) {
            var orderbook = response.getTrade();
            var figi = orderbook.getFigi();
            var time = orderbook.getTimestamp();
            repository.addTrade(figi, time);
        }
    }

    @Override
    public String streamName() {
        return TRADES;
    }
}
