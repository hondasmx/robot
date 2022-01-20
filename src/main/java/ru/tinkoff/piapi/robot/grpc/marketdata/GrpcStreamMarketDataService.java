package ru.tinkoff.piapi.robot.grpc.marketdata;

import io.grpc.ManagedChannel;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.CandleInstrument;
import ru.tinkoff.piapi.contract.v1.InfoInstrument;
import ru.tinkoff.piapi.contract.v1.MarketDataRequest;
import ru.tinkoff.piapi.contract.v1.MarketDataResponse;
import ru.tinkoff.piapi.contract.v1.MarketDataStreamServiceGrpc;
import ru.tinkoff.piapi.contract.v1.OrderBookInstrument;
import ru.tinkoff.piapi.contract.v1.SubscribeCandlesRequest;
import ru.tinkoff.piapi.contract.v1.SubscribeInfoRequest;
import ru.tinkoff.piapi.contract.v1.SubscribeOrderBookRequest;
import ru.tinkoff.piapi.contract.v1.SubscribeTradesRequest;
import ru.tinkoff.piapi.contract.v1.SubscriptionAction;
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval;
import ru.tinkoff.piapi.contract.v1.TradeInstrument;
import ru.tinkoff.piapi.robot.grpc.BaseService;
import ru.tinkoff.piapi.robot.processor.CandlesProcessor;
import ru.tinkoff.piapi.robot.processor.InfoProcessor;
import ru.tinkoff.piapi.robot.processor.MarketdataStreamProcessor;
import ru.tinkoff.piapi.robot.processor.OrderbookProcessor;
import ru.tinkoff.piapi.robot.processor.TradesProcessor;
import ru.tinkoff.piapi.robot.services.events.StreamErrorEvent;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class GrpcStreamMarketDataService extends BaseService<MarketDataStreamServiceGrpc.MarketDataStreamServiceStub> {

    private final CandlesProcessor candlesProcessor;
    private final InfoProcessor infoProcessor;
    private final OrderbookProcessor orderbookProcessor;
    private final TradesProcessor tradesProcessor;
    private ManagedChannel managedChannel;
    private final ApplicationEventPublisher publisher;


    @Override
    protected MarketDataStreamServiceGrpc.MarketDataStreamServiceStub getStub() {
        managedChannel = managedChannel();
        return MarketDataStreamServiceGrpc
                .newStub(managedChannel)
                .withInterceptors(MetadataUtils.newCaptureMetadataInterceptor(headersCapture, trailersCapture));
    }

    public void candlesStream(Set<String> figis) {
        log.info("candles subscription on {} figi", figis.size());
        var subscriptionAction = SubscriptionAction.SUBSCRIPTION_ACTION_SUBSCRIBE;
        var candlesBuilder = SubscribeCandlesRequest
                .newBuilder()
                .setSubscriptionAction(subscriptionAction);
        for (String figi : figis) {
            candlesBuilder.addInstruments(CandleInstrument
                    .newBuilder()
                    .setInterval(SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE)
                    .setFigi(figi)
                    .build());
        }
        var request = MarketDataRequest
                .newBuilder()
                .setSubscribeCandlesRequest(candlesBuilder)
                .build();
        var observer = getStubWithHeaders().marketDataStream(new MarketDataStreamObserver(candlesProcessor));
        observer.onNext(request);
    }

    public void tradesStream(Set<String> figis) {
        log.info("trades subscription on {} figi", figis.size());
        var subscriptionAction = SubscriptionAction.SUBSCRIPTION_ACTION_SUBSCRIBE;
        var orderBookBuilder = SubscribeTradesRequest
                .newBuilder()
                .setSubscriptionAction(subscriptionAction);
        for (String figi : figis) {
            orderBookBuilder.addInstruments(TradeInstrument
                    .newBuilder()
                    .setFigi(figi)
                    .build());
        }
        var request = MarketDataRequest
                .newBuilder()
                .setSubscribeTradesRequest(orderBookBuilder)
                .build();
        var observer = getStubWithHeaders().marketDataStream(new MarketDataStreamObserver(tradesProcessor));
        observer.onNext(request);
    }

    public void orderBookStream(Set<String> figis) {
        log.info("orderbook subscription on {} figi", figis.size());
        var subscriptionAction = SubscriptionAction.SUBSCRIPTION_ACTION_SUBSCRIBE;
        var orderBookBuilder = SubscribeOrderBookRequest
                .newBuilder()
                .setSubscriptionAction(subscriptionAction);
        for (String figi : figis) {
            orderBookBuilder.addInstruments(OrderBookInstrument
                    .newBuilder()
                    .setDepth(50)
                    .setFigi(figi)
                    .build());
        }
        var request = MarketDataRequest
                .newBuilder()
                .setSubscribeOrderBookRequest(orderBookBuilder)
                .build();
        var observer = getStubWithHeaders().marketDataStream(new MarketDataStreamObserver(orderbookProcessor));
        observer.onNext(request);
    }

    public void infoStream(Set<String> figis) {
        log.info("subscribing on trading_status on {} figis", figis.size());
        var subscriptionAction = SubscriptionAction.SUBSCRIPTION_ACTION_SUBSCRIBE;
        var candlesBuilder = SubscribeInfoRequest
                .newBuilder()
                .setSubscriptionAction(subscriptionAction);
        for (String figi : figis) {
            candlesBuilder.addInstruments(InfoInstrument.newBuilder().setFigi(figi).build());
        }
        var request = MarketDataRequest
                .newBuilder()
                .setSubscribeInfoRequest(candlesBuilder)
                .build();
        var observer = getStubWithHeaders().marketDataStream(new MarketDataStreamObserver(infoProcessor));
        observer.onNext(request);
    }

    public void shutdown() {
        if (managedChannel != null) {
            managedChannel.shutdownNow();
            try {
                managedChannel.awaitTermination(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class FigiDepth {
        private String figi;
        private int depth;
    }

    @Data
    @AllArgsConstructor
    public static class FigiInterval {
        private String figi;
        private SubscriptionInterval interval;
    }

    @Getter
    public class MarketDataStreamObserver implements StreamObserver<MarketDataResponse> {
        private final MarketdataStreamProcessor streamProcessor;


        public MarketDataStreamObserver(MarketdataStreamProcessor streamProcessor) {
            this.streamProcessor = streamProcessor;

        }

        @Override
        public void onNext(MarketDataResponse value) {
            streamProcessor.process(value);
        }

        @Override
        public void onError(Throwable t) {
            log.error("onError was invoked. stream: {}, error: {}", streamProcessor.streamName(), t.toString());
            shutdown();
            publisher.publishEvent(new StreamErrorEvent(streamProcessor.streamName()));
        }

        @Override
        public void onCompleted() {
            log.info("onCompleted was invoked. stream: {}", streamProcessor.streamName());
        }
    }
}
