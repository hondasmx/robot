package ru.tinkoff.piapi.robot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.robot.db.repositories.InstrumentRepository;
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
@RequiredArgsConstructor
public class StreamService {

    private final InstrumentRepository instrumentRepository;
    private final GrpcStreamMarketDataService candlesGrpcService;
    private final GrpcStreamMarketDataService orderbookGrpcService;
    private final GrpcStreamMarketDataService tradesGrpcService;
    private final GrpcStreamMarketDataService infoGrpcService;


    private ExecutorService infoStreamExecutorService = Executors.newFixedThreadPool(2);
    private ExecutorService tradesExecutorService = Executors.newFixedThreadPool(2);
    private ExecutorService orderbookExecutorService = Executors.newFixedThreadPool(2);
    private ExecutorService candlesExecutorService = Executors.newFixedThreadPool(2);
    private Set<String> normalTradingFigi = Collections.synchronizedSet(new HashSet<>());
    private Set<String> allFigi = Collections.synchronizedSet(new HashSet<>());
    private List<TradingStatusChangedEvent> newFigi = Collections.synchronizedList(new ArrayList<>());


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
        infoStreamExecutorService.execute(() -> infoGrpcService.infoStream(allFigi));
    }

    public void initCandlesStream() {
        candlesExecutorService.shutdownNow();
        candlesExecutorService = Executors.newFixedThreadPool(2);
        candlesExecutorService.execute(() -> candlesGrpcService.candlesStream(normalTradingFigi));
    }

    public void initOrderbookStream() {
        orderbookExecutorService.shutdownNow();
        orderbookExecutorService = Executors.newFixedThreadPool(2);
        orderbookExecutorService.execute(() -> orderbookGrpcService.orderBookStream(normalTradingFigi));
    }

    public void initTradesStream() {
        tradesExecutorService.shutdownNow();
        tradesExecutorService = Executors.newFixedThreadPool(2);
        tradesExecutorService.execute(() -> tradesGrpcService.tradesStream(normalTradingFigi));
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
            var time = tradingStatusChangedEvent.getTime().toString();
            if (isNormalTrading(status)) {
                if (normalTradingFigi.add(figi)) {
                    log.info("trading status was changed to normal for figi {}. Time {}", figi, time);
                    needToRefreshStream = true;
                }
            } else {
                if (normalTradingFigi.remove(figi)) {
                    log.info("trading status was changed to {} for figi {}. Time {}", status, figi, time);
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
