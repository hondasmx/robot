package ru.tinkoff.piapi.robot.services.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.robot.db.repositories.CandlesRepository;
import ru.tinkoff.piapi.robot.db.repositories.InstrumentRepository;
import ru.tinkoff.piapi.robot.db.repositories.OrderbookRepository;
import ru.tinkoff.piapi.robot.db.repositories.TradeRepository;
import ru.tinkoff.piapi.robot.utils.DateUtils;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamScheduler {

    private static final List<String> instrumentTypes = List.of("share", "currency", "futures", "bond", "etf");
    private static final long MINIMUM_DELAY = 10_000L;
    private final InstrumentRepository instrumentRepository;
    private final CandlesRepository candlesRepository;
    private final OrderbookRepository orderbookRepository;
    private final TradeRepository tradeRepository;

    //    @Scheduled(fixedRate = 1000 * 60)
    public void candlesChecker() {
        for (String instrumentType : instrumentTypes) {
            var timestamp = candlesRepository.lastCandle(instrumentType);
            if (timestamp == null) {
//                log.info("last candle not found for instrumentType {}", instrumentType);
                continue;
            }
            var lastCandle = timestamp.getTime();
            var now = Instant.now().toEpochMilli();
            var delay = now - lastCandle;
            if (delay >= MINIMUM_DELAY) {
                log.info("last candle for instrumentType {}, time ago {}", instrumentType, DateUtils.millisToString(delay));
            }
        }
    }

    //    @Scheduled(fixedRate = 1000 * 60)
    public void tradesChecker() {
        for (String instrumentType : instrumentTypes) {
            var timestamp = tradeRepository.lastTrade(instrumentType);
            if (timestamp == null) {
//                log.info("last trade not found for instrumentType {}", instrumentType);
                continue;
            }
            var lastTrade = timestamp.getTime();
            var now = Instant.now().toEpochMilli();
            var delay = now - lastTrade;
            if (delay >= MINIMUM_DELAY) {
                log.info("last trade for instrumentType {}, time ago {}", instrumentType, DateUtils.millisToString(delay));
            }
        }
    }

    //    @Scheduled(fixedRate = 1000 * 60)
    public void orderbookChecker() {
        for (String instrumentType : instrumentTypes) {
            var timestamp = orderbookRepository.lastOrderbook(instrumentType);
            if (timestamp == null) {
//                log.info("last orderbook not found for instrumentType {}", instrumentType);
                continue;
            }
            var lastOrderbook = timestamp.getTime();
            var now = Instant.now().toEpochMilli();
            var delay = now - lastOrderbook;
            if (delay >= MINIMUM_DELAY) {
                log.info("last orderbook for instrumentType {}, time ago {}", instrumentType, DateUtils.millisToString(delay));
            }
        }
    }
}
