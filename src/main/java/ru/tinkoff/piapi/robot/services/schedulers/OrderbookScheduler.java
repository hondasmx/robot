package ru.tinkoff.piapi.robot.services.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.robot.db.repositories.OrderbookRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderbookScheduler {

    private final OrderbookRepository orderbookRepository;

    // Проверяем, что в стакане bid < ask
    @Scheduled(fixedRate = 1000 * 60 * 10, initialDelay = 1000 * 60 * 6)
    public void failedOrderbookScheduler() {
        log.debug("job started: failedOrderbookScheduler");
        var failedOrderbook = orderbookRepository.failedOrderbook();
        if (failedOrderbook.size() > 0) {
            log.error("orderbook has bid > ask");
        }
        log.debug("job finished: failedOrderbookScheduler");
    }
}
