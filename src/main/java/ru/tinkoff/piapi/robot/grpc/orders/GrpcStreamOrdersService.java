package ru.tinkoff.piapi.robot.grpc.orders;

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
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.robot.grpc.BaseService;
import ru.tinkoff.piapi.robot.grpc.marketdata.GrpcStreamMarketDataService;
import ru.tinkoff.piapi.robot.processor.marketdata.*;
import ru.tinkoff.piapi.robot.processor.orders.OrdersProcessor;
import ru.tinkoff.piapi.robot.services.events.StreamErrorEvent;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class GrpcStreamOrdersService extends BaseService<OrdersStreamServiceGrpc.OrdersStreamServiceStub> {

    private final OrdersProcessor ordersProcessor;
    private ManagedChannel managedChannel;
    public final ApplicationEventPublisher publisher;


    @Override
    protected OrdersStreamServiceGrpc.OrdersStreamServiceStub getStub() {
        managedChannel = managedChannel();
        return OrdersStreamServiceGrpc
                .newStub(managedChannel)
                .withInterceptors(MetadataUtils.newCaptureMetadataInterceptor(headersCapture, trailersCapture));
    }

    public void ordersStream() {
        log.info("orders subscription");
        var request = TradesStreamRequest
                .newBuilder()
                .build();
        getStubWithHeaders().tradesStream(request, new MarketDataStreamObserver(ordersProcessor));
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

    @Getter
    public class MarketDataStreamObserver implements StreamObserver<TradesStreamResponse> {
        private final OrdersProcessor streamProcessor;


        public MarketDataStreamObserver(OrdersProcessor streamProcessor) {
            this.streamProcessor = streamProcessor;

        }

        @Override
        public void onNext(TradesStreamResponse value) {
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
