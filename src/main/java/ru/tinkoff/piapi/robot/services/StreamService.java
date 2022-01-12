package ru.tinkoff.piapi.robot.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.robot.db.repositories.InstrumentRepository;
import ru.tinkoff.piapi.robot.grpc.StreamConfiguration;
import ru.tinkoff.piapi.robot.grpc.marketdata.GrpcStreamMarketDataService;
import ru.tinkoff.piapi.robot.processor.MarketdataStreamNames;
import ru.tinkoff.piapi.robot.services.events.StreamErrorEvent;
import ru.tinkoff.piapi.robot.services.events.TradingStatusChangedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class StreamService {

    private final InstrumentRepository instrumentRepository;
    private final GrpcStreamMarketDataService streamMarketDataService;
    private final List<TradingStatusChangedEvent> newFigi = Collections.synchronizedList(new ArrayList<>());
    private ExecutorService infoStreamExecutorService;
    private ExecutorService tradesExecutorService;
    private ExecutorService orderbookExecutorService;
    private ExecutorService candlesExecutorService;
    private Set<String> normalTradingFigi = Collections.synchronizedSet(new HashSet<>());
    private Set<String> allFigi = Collections.synchronizedSet(new HashSet<>());

    public StreamService(InstrumentRepository instrumentRepository, GrpcStreamMarketDataService streamMarketDataService, StreamConfiguration streamConfiguration) {
        this.instrumentRepository = instrumentRepository;
        this.streamMarketDataService = streamMarketDataService;
        infoStreamExecutorService = Executors.newFixedThreadPool(2);
        tradesExecutorService = Executors.newFixedThreadPool(2);
        orderbookExecutorService = Executors.newFixedThreadPool(2);
        candlesExecutorService = Executors.newFixedThreadPool(2);
    }

    public void collectFigi() {
        normalTradingFigi = new HashSet<>(instrumentRepository.figiByTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING.name()));
        allFigi = new HashSet<>(instrumentRepository.findAll());
        log.info("normal_trading figi size {}", normalTradingFigi.size());
        log.info("all figi size {}", allFigi.size());
    }

    @EventListener(StreamErrorEvent.class)
    public void recreateStream(StreamErrorEvent event) {
        var streamName = event.getStreamName();
        log.info("recreating stream: {}", streamName);
        if (MarketdataStreamNames.ORDERBOOK.equals(streamName)) {
            initOrderbookStream();
        } else if (MarketdataStreamNames.CANDLES.equals(streamName)) {
            initCandlesStream();
        } else if (MarketdataStreamNames.INFO.equals(streamName)) {
            initInfoStream();
        } else if (MarketdataStreamNames.TRADES.equals(streamName)) {
            initTradesStream();
        }
    }

    public void initInfoStream() {
        infoStreamExecutorService.shutdownNow();
        infoStreamExecutorService = Executors.newFixedThreadPool(2);
        infoStreamExecutorService.execute(() -> streamMarketDataService.infoStream(allFigi));
    }

    public void initCandlesStream() {
        candlesExecutorService.shutdownNow();
        candlesExecutorService = Executors.newFixedThreadPool(2);
        candlesExecutorService.execute(() -> streamMarketDataService.candlesStream(normalTradingFigi));
    }

    public void initOrderbookStream() {
        orderbookExecutorService.shutdownNow();
        orderbookExecutorService = Executors.newFixedThreadPool(2);
        orderbookExecutorService.execute(() -> streamMarketDataService.orderBookStream(normalTradingFigi));
    }

    public void initTradesStream() {
        tradesExecutorService.shutdownNow();
        tradesExecutorService = Executors.newFixedThreadPool(2);
        tradesExecutorService.execute(() -> streamMarketDataService.tradesStream(normalTradingFigi));
    }

    public void initMDStreams() {
        initOrderbookStream();
        initCandlesStream();
        initTradesStream();
    }


    @Scheduled(fixedRate = 1000 * 60)
    public void updateStreams() {
        if (newFigi.size() == 0) {
            return;
        }
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
}
