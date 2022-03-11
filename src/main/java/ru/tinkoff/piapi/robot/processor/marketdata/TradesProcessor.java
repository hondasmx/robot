package ru.tinkoff.piapi.robot.processor.marketdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.MarketDataResponse;
import ru.tinkoff.piapi.contract.v1.SubscriptionStatus;
import ru.tinkoff.piapi.robot.db.repositories.TradeRepository;
import ru.tinkoff.piapi.robot.processor.StreamProcessor;
import ru.tinkoff.piapi.robot.utils.MoneyUtils;

import static ru.tinkoff.piapi.robot.processor.StreamNames.TRADES;

@Component
@Slf4j
@RequiredArgsConstructor
public class TradesProcessor implements StreamProcessor<MarketDataResponse> {

    private final TradeRepository repository;


    @Override
    public void process(MarketDataResponse response) {
        if (response.hasTrade()) {
            var trade = response.getTrade();
            var figi = trade.getFigi();
            var time = trade.getTime();
            var direction = trade.getDirection().name();
            var lot = trade.getQuantity();
            var price = MoneyUtils.quotationToBigDecimal(trade.getPrice());
            repository.addTrade(figi, time, direction, lot, price);
        } else if (response.hasSubscribeTradesResponse()) {
            var count = response.getSubscribeTradesResponse().getTradeSubscriptionsList().stream().filter(el -> el.getSubscriptionStatus().equals(SubscriptionStatus.SUBSCRIPTION_STATUS_SUCCESS)).count();
            log.info("success trades subscriptions: {}", count);
        }
    }

    @Override
    public String streamName() {
        return TRADES;
    }
}
