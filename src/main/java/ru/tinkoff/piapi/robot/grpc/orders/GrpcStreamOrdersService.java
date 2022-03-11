package ru.tinkoff.piapi.robot.grpc.orders;

import io.grpc.ManagedChannel;
import io.grpc.stub.MetadataUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.OrdersStreamServiceGrpc;
import ru.tinkoff.piapi.contract.v1.TradesStreamRequest;
import ru.tinkoff.piapi.contract.v1.TradesStreamResponse;
import ru.tinkoff.piapi.robot.grpc.BaseService;
import ru.tinkoff.piapi.robot.processor.StreamProcessor;
import ru.tinkoff.piapi.robot.processor.orders.OrdersProcessor;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class GrpcStreamOrdersService extends BaseService<OrdersStreamServiceGrpc.OrdersStreamServiceStub> {

    public final ApplicationEventPublisher publisher;
    private final OrdersProcessor ordersProcessor;
    private ManagedChannel managedChannel;

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
        getStubWithHeaders().tradesStream(request, new OrdersStreamObserver<>(ordersProcessor, publisher, this));
    }

    @Override
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
}
