package ru.tinkoff.piapi.robot.services.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.robot.grpc.marketdata.GrpcPublicMarketdataService;
import ru.tinkoff.piapi.robot.services.StreamService;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static ru.tinkoff.piapi.robot.utils.DateUtils.secondsToString;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketdataLastPricesScheduler {

    private final GrpcPublicMarketdataService grpcPublicMarketdataService;
    private final Map<String, Long> result = new HashMap<>();
    private final StreamService streamService;

    @Scheduled(fixedRate = 1000 * 60 * 5, initialDelay = 1000 * 60 * 5)
    public void lastPriceCheck() {
        log.info("job started: lastPriceCheck");
        var lastPrices = grpcPublicMarketdataService.getLastPrices();
        var failedFigi = 0;
        for (LastPrice lastPrice : lastPrices) {
            var figi = lastPrice.getFigi();
            var time = lastPrice.getTime();
            var newSeconds = time.getSeconds();
            if (result.containsKey(figi)) {
                var previousSeconds = result.get(figi);
                if (newSeconds < previousSeconds) {
                    failedFigi++;
                    log.error("new time is less then previous. example {}", figi);
                }
            } else {
                result.put(figi, newSeconds);
            }
        }
//        log.error("new time is less then previous. example {}", result.keySet());
    }

    /**
     * Проверяем, что по всем бумагам с normal_trading есть last_prices
     */
//    @Scheduled(fixedRate = 1000 * 60 * 5, initialDelay = 1000 * 60 * 5)
    public void lastPriceCheck2() {
        log.info("job started: lastPriceCheck2");
        var lastPrices = grpcPublicMarketdataService.getLastPrices();
        var normalTradingFigi = new HashSet<>(streamService.normalTradingFigi);
        for (LastPrice lastPrice : lastPrices) {
            var figi = lastPrice.getFigi();
            if (normalTradingFigi.contains(figi)) {
                var time = lastPrice.getTime().getSeconds();
                var now = Instant.now().toEpochMilli() / 1000;
                var diff = now - time;
                if (diff > 60) {
                    log.error("last price is too old (>60sec). figi {}, diff {}", figi, secondsToString(diff));
                }
            }
        }
    }
}
