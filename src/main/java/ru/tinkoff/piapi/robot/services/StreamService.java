package ru.tinkoff.piapi.robot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.robot.db.repositories.InstrumentRepository;
import ru.tinkoff.piapi.robot.db.repositories.TradingStatusRepository;
import ru.tinkoff.piapi.robot.grpc.marketdata.GrpcStreamMarketDataService;
import ru.tinkoff.piapi.robot.grpc.orders.GrpcStreamOrdersService;
import ru.tinkoff.piapi.robot.processor.StreamNames;
import ru.tinkoff.piapi.robot.services.events.StreamErrorEvent;
import ru.tinkoff.piapi.robot.services.events.TradingStatusChangedEvent;
import ru.tinkoff.piapi.robot.utils.DateUtils;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static ru.tinkoff.piapi.contract.v1.SecurityTradingStatus.SECURITY_TRADING_STATUS_SESSION_OPEN;

@Component
@Slf4j
@RequiredArgsConstructor
public class StreamService {

    private final InstrumentRepository instrumentRepository;
    private final TradingStatusRepository tradingStatusRepository;
    private final GrpcStreamMarketDataService candlesGrpcService;
    private final GrpcStreamMarketDataService orderbookGrpcService;
    private final GrpcStreamMarketDataService tradesGrpcService;
    private final GrpcStreamMarketDataService infoGrpcService;
    private final GrpcStreamOrdersService ordersGrpcService;
    private final long DEFAULT_DELAY = 5000;
    public Set<String> normalTradingFigi = Collections.synchronizedSet(new HashSet<>());
    private ScheduledExecutorService infoStreamExecutorService = Executors.newScheduledThreadPool(2);
    private ScheduledExecutorService tradesExecutorService = Executors.newScheduledThreadPool(2);
    private ScheduledExecutorService orderbookExecutorService = Executors.newScheduledThreadPool(2);
    private ScheduledExecutorService candlesExecutorService = Executors.newScheduledThreadPool(2);
    private ScheduledExecutorService ordersExecutorService = Executors.newScheduledThreadPool(2);
    private Set<String> allFigi = Collections.synchronizedSet(new HashSet<>());
    private List<TradingStatusChangedEvent> newFigi = Collections.synchronizedList(new ArrayList<>());


    public void collectFigi() {
        normalTradingFigi = new HashSet<>(tradingStatusRepository.figiByTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING.name()));
        allFigi = new HashSet<>(instrumentRepository.findAll());
        log.info("normal_trading figi size {}", normalTradingFigi.size());
        log.info("all figi size {}", allFigi.size());
    }

    @EventListener(StreamErrorEvent.class)
    public void recreateStream(StreamErrorEvent event) {
        var streamName = event.getStreamName();
        log.info("recreating stream: {}", streamName);
        if (StreamNames.ORDERBOOK.equals(streamName)) {
            initOrderbookStream();
        } else if (StreamNames.CANDLES.equals(streamName)) {
            initCandlesStream();
        } else if (StreamNames.INFO.equals(streamName)) {
            initInfoStream();
        } else if (StreamNames.TRADES.equals(streamName)) {
            initTradesStream();
        } else if (StreamNames.ORDERS.equals(streamName)) {
            initOrdersStream();
        }
    }

    public void initInfoStream() {
        infoGrpcService.shutdown();
        infoStreamExecutorService.shutdownNow();
        infoStreamExecutorService = Executors.newScheduledThreadPool(2);
        infoStreamExecutorService.schedule(() -> infoGrpcService.infoStream(allFigi), DEFAULT_DELAY, TimeUnit.MILLISECONDS);
    }

    public void initCandlesStream() {
        if (normalTradingFigi.size() == 0) {
            log.info("normal trading figi size = 0. abort CANDLES stream initiation");
            return;
        }
        candlesGrpcService.shutdown();
        candlesExecutorService.shutdownNow();
        candlesExecutorService = Executors.newScheduledThreadPool(2);
        candlesExecutorService.schedule(() -> candlesGrpcService.candlesStream(normalTradingFigi), DEFAULT_DELAY, TimeUnit.MILLISECONDS);
    }

    public void initOrderbookStream() {
        if (normalTradingFigi.size() == 0) {
            log.info("normal trading figi size = 0. abort ORDERBOOK stream initiation");
            return;
        }

        orderbookGrpcService.shutdown();
        orderbookExecutorService.shutdownNow();
        orderbookExecutorService = Executors.newScheduledThreadPool(2);
        orderbookExecutorService.schedule(() -> orderbookGrpcService.orderBookStream(normalTradingFigi), DEFAULT_DELAY, TimeUnit.MILLISECONDS);
    }

    public void initTradesStream() {
        if (normalTradingFigi.size() == 0) {
            log.info("normal trading figi size = 0. abort TRADES stream initiation");
            return;
        }

        tradesGrpcService.shutdown();
        tradesExecutorService.shutdownNow();
        tradesExecutorService = Executors.newScheduledThreadPool(2);
        tradesExecutorService.schedule(() -> tradesGrpcService.tradesStream(normalTradingFigi), DEFAULT_DELAY, TimeUnit.MILLISECONDS);
    }

    public void initOrdersStream() {
        ordersGrpcService.shutdown();
        ordersExecutorService.shutdownNow();
        ordersExecutorService = Executors.newScheduledThreadPool(2);
        ordersExecutorService.schedule(ordersGrpcService::ordersStream, DEFAULT_DELAY, TimeUnit.MILLISECONDS);
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
        var cloned = new HashSet<>(newFigi);
        for (TradingStatusChangedEvent event : cloned) {
            var figi = event.getFigi();
            var status = event.getTradingStatus();
            var time = DateUtils.timestampToDate(event.getTradingStatusUpdatedAt()).toString();
            if (isNormalTrading(status)) {
                if (!normalTradingFigi.contains(figi)) {
                    log.info("trading status was changed to normal for figi {}. Time {}", figi, time);
                    needToRefreshStream = true;
                }
            } else {
                if (normalTradingFigi.contains(figi)) {
                    log.info("trading status was changed to {} for figi {}. Time {}", status, figi, time);
                    needToRefreshStream = true;
                }
            }
        }
        newFigi.removeAll(cloned);
        if (needToRefreshStream) {
            log.info("need to resubscribe");
            collectFigi();
            initMDStreams();
        }
    }

    private boolean isNormalTrading(String status) {
        return SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING.name().equals(status) || SECURITY_TRADING_STATUS_SESSION_OPEN.name().equals(status);
    }

    @EventListener(TradingStatusChangedEvent.class)
    public void tradingStatusChanged(TradingStatusChangedEvent event) {
        newFigi.add(event);
    }
}
