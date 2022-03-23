package ru.tinkoff.piapi.robot.services.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.robot.db.repositories.FuturesMarginRepository;
import ru.tinkoff.piapi.robot.db.repositories.InstrumentRepository;
import ru.tinkoff.piapi.robot.grpc.SdkService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FuturesMarginScheduler {

    private final FuturesMarginRepository futuresMarginRepository;
    private final InstrumentRepository instrumentRepository;
    private final SdkService sdkService;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);


    @Scheduled(fixedRate = 1000 * 60 * 60, initialDelay = 1000 * 60 * 15)
    public void futuresMarginInit() {
        log.debug("job started: futuresMarginInit");
        var futures = instrumentRepository.figiByInstrumentType("futures");
        for (String figi : futures) {
            //На энвое есть лимит в 200 rpm, поэтому делаем запросы не чаще 2rps
            executor.scheduleAtFixedRate(() -> {
                var futuresMargin = sdkService.getInvestApi().getInstrumentsService().getFuturesMarginSync(figi);
                futuresMarginRepository.addFutureMargin(figi, futuresMargin);
            }, 0, 500, TimeUnit.MILLISECONDS);

        }
        log.debug("job finished: futuresMarginInit");
    }
}
