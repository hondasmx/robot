package ru.tinkoff.piapi.robot.services.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.robot.db.repositories.OrderbookRepository;
import ru.tinkoff.piapi.robot.db.repositories.impl.OrderbookRepositoryImpl;
import ru.tinkoff.piapi.robot.services.StreamService;
import ru.tinkoff.piapi.robot.services.TelegramService;

import java.text.MessageFormat;
import java.util.HashSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderbookScheduler {

    private final OrderbookRepository orderbookRepository;
    private final TelegramService telegramService;
    private final StreamService streamService;

    // Проверяем, что в стакане bid < ask
    @Scheduled(fixedRate = 1000 * 60 * 10, initialDelay = 1000 * 60 * 6)
    public void failedOrderbookScheduler() {
        log.debug("job started: failedOrderbookScheduler");
        var failedOrderbook = new HashSet<>(orderbookRepository.failedOrderbook());
        if (failedOrderbook.size() > 0) {
            log.error("orderbook has bid > ask");
            var message = new StringBuilder("Стакан содержит bid > ask \n\n");
            for (OrderbookRepositoryImpl.OrderbookResponse response : failedOrderbook) {
                message.append(MessageFormat.format("figi: {0}, ask: {1}, bid: {2}, createdAt: {3} \n", response.getFigi(), response.getAsk(), response.getBid(), response.getCreatedAt()));
            }
            telegramService.sendMessage(message.toString());
        }
        log.debug("job finished: failedOrderbookScheduler");
    }

    // Проверяем, что time в стакане отличается от now не больше, чем на 5 минут
    @Scheduled(fixedRate = 1000 * 60 * 10, initialDelay = 1000 * 60 * 6)
    public void timeDiffScheduler() {
        log.debug("job started: timeDiffScheduler");
        var timeDiffOrderbook = orderbookRepository.timeDiffOrderbook();
        if (timeDiffOrderbook.size() > 0) {
            var builder = new StringBuilder("Стакан пришел с задержкой >= 5 минут \n\n");
            for (OrderbookRepositoryImpl.TimeDiffResponse response : timeDiffOrderbook) {
                var normalTradingFigi = new HashSet<>(streamService.normalTradingFigi);
                var figi = response.getFigi();
                if (!normalTradingFigi.contains(figi)) {
                    continue;
                }
                builder.append(MessageFormat.format("figi: {0}, timestamp: {1}, diff: {2} \n", figi, response.getTimestamp(), response.getDiff()));
            }
            var message = builder.toString();
            log.error(message);
            telegramService.sendMessage(message);
        }
        log.debug("job finished: timeDiffScheduler");
    }

    // Проверяем, что last price находится в пределах limitUp / limitDown
    @Scheduled(fixedRate = 1000 * 60 * 60, initialDelay = 1000 * 60 * 6)
    public void limitUpLimitDownChecker() {
        log.debug("job started: limitUpLimitDownChecker");
        var failedLimits = orderbookRepository.failedLimits();
        if (failedLimits.size() > 0) {
            var builder = new StringBuilder("Bid/ask находятся за лимитами \n\n");
            for (OrderbookRepositoryImpl.OrderbookResponse response : failedLimits) {
                builder.append(MessageFormat.format("figi: {0}, timestamp: {1}, limitup: {2}, limitdown: {3} \n", response.getFigi(), response.getTimestamp(), response.getLimitUp(), response.getLimitDown()));
            }
            var message = builder.toString();
            log.error(message);
            telegramService.sendMessage(message);
        }
        log.debug("job finished: limitUpLimitDownChecker");
    }
}
