package ru.tinkoff.piapi.robot.services.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.robot.db.repositories.CandlesRepository;
import ru.tinkoff.piapi.robot.services.TelegramService;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketdataScheduler {

    private final CandlesRepository candlesRepository;
    private final TelegramService telegramService;


    @Scheduled(fixedRate = 1000 * 60 * 5)
    public void candlesStreamAlive() {
        log.info("job started: candlesStreamAlive");
        var lastCandleDate = candlesRepository.lastCandle();
        log.info("lastCandleDate to string {}", lastCandleDate.toString());
        log.info("lastCandle instant {}", lastCandleDate.toInstant().toString());
        log.info("now instant {}", Instant.now().toString());
        log.info("instant diff {}", Instant.now().compareTo(lastCandleDate.toInstant()));
    }
}
