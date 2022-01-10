package ru.tinkoff.piapi.robot.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.robot.db.repositories.InstrumentRepository;
import ru.tinkoff.piapi.robot.grpc.StreamConfiguration;
import ru.tinkoff.piapi.robot.grpc.marketdata.GrpcStreamMarketDataService;
import ru.tinkoff.piapi.robot.services.events.TradingStatusChangedEvent;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class StreamService {

    private final InstrumentRepository instrumentRepository;
    private final GrpcStreamMarketDataService streamMarketDataService;
    private final int threadPoolSize;
    private final List<TradingStatusChangedEvent> newFigi = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService infoStreamExecutorService;
    private ExecutorService executorService;
    private Set<String> normalTradingFigi = Collections.synchronizedSet(new HashSet<>());
    private Set<String> allFigi = Collections.synchronizedSet(new HashSet<>());

    public StreamService(InstrumentRepository instrumentRepository, GrpcStreamMarketDataService streamMarketDataService, StreamConfiguration streamConfiguration) {
        this.instrumentRepository = instrumentRepository;
        this.streamMarketDataService = streamMarketDataService;
        threadPoolSize = streamConfiguration.getCandles().getStreamCount() + streamConfiguration.getOrderbook().getStreamCount() + streamConfiguration.getTrades().getStreamCount() + 1;
        infoStreamExecutorService = Executors.newFixedThreadPool(2);
        collectFigi();
    }

    private void collectFigi() {
        normalTradingFigi = new HashSet<>(instrumentRepository.figiByTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING.name()));
        allFigi = new HashSet<>(instrumentRepository.findAll());
        log.info("normal_trading figi size {}", normalTradingFigi.size());
        log.info("all figi size {}", allFigi.size());
    }

    public void initInfoStream() {
        infoStreamExecutorService.execute(() -> streamMarketDataService.infoStream(allFigi));
    }

    public void initMDStreams() {
        collectFigi();
        refreshExecutorService();
        orderbookStream();
        candlesStream();
        tradesStream();
    }


    @Scheduled(fixedRate = 1000 * 60)
    public void updateStreams() {
        if (newFigi.size() == 0) return;
        var needToRefreshStream = false;
        for (TradingStatusChangedEvent tradingStatusChangedEvent : newFigi) {
            var figi = tradingStatusChangedEvent.getFigi();
            var status = tradingStatusChangedEvent.getTradingStatus();
            if (isNormalTrading(status)) {
                if (normalTradingFigi.add(figi)) {
                    log.info("trading status was changed to normal for figi {}", figi);
                    needToRefreshStream = true;
                }
            } else {
                if (normalTradingFigi.remove(figi)) {
                    log.info("trading status was changed to {} for figi {}", status, figi);
                    needToRefreshStream = true;
                }
            }
        }
        newFigi.clear();
        if (needToRefreshStream) {
            log.info("need to resubscribe");
            initMDStreams();
        }
    }

    private boolean isNormalTrading(String status) {
        return SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING.name().equals(status);
    }

    @EventListener(TradingStatusChangedEvent.class)
    public void tradingStatusChanged(TradingStatusChangedEvent event) {
        newFigi.add(event);
    }

    private void candlesStream() {
        executorService.execute(() -> streamMarketDataService.candlesStream(normalTradingFigi));
    }

    private void orderbookStream() {
        executorService.execute(() -> streamMarketDataService.orderBookStream(normalTradingFigi));
    }

    private void tradesStream() {
        executorService.execute(() -> streamMarketDataService.tradesStream(normalTradingFigi));
    }

    private void refreshExecutorService() {
        log.info("recreating executor service. threadPoolSize {}", threadPoolSize);
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(threadPoolSize);
        }
        executorService.shutdown();
        executorService = Executors.newFixedThreadPool(threadPoolSize);
    }
}
