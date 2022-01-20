package ru.tinkoff.piapi.robot.grpc.orders;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.GetOrdersRequest;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderState;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.OrdersServiceGrpc;
import ru.tinkoff.piapi.contract.v1.PostOrderRequest;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;
import ru.tinkoff.piapi.robot.grpc.BaseService;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class GrpcPublicOrdersService extends BaseService<OrdersServiceGrpc.OrdersServiceBlockingStub> {

    private ManagedChannel managedChannel;
    @Value("${auth.account}")
    private String accountId;

    @Override
    protected OrdersServiceGrpc.OrdersServiceBlockingStub getStub() {
        managedChannel = managedChannel();
        return OrdersServiceGrpc
                .newBlockingStub(managedChannel)
                .withInterceptors(MetadataUtils.newCaptureMetadataInterceptor(headersCapture, trailersCapture));
    }

    public List<OrderState> getOrders() {
        var request = GetOrdersRequest.newBuilder().setAccountId(accountId).build();
        var body = getStubWithHeaders().getOrders(request);
        managedChannel.shutdownNow();
        return getResponse(body).getResponse().getOrdersList();
    }

    public PostOrderResponse postOrder(String figi) {
        var orderId = UUID.randomUUID().toString();
        var request = PostOrderRequest.newBuilder()
                .setAccountId(accountId)
                .setFigi(figi)
                .setDirection(OrderDirection.ORDER_DIRECTION_BUY)
                .setOrderType(OrderType.ORDER_TYPE_MARKET)
                .setQuantity(1L)
                .setOrderId(orderId)
                .build();
        try {
            var body = getStubWithHeaders().postOrder(request);
            return getResponse(body).getResponse();

        } catch (StatusRuntimeException exception) {
            var errorMessage = exception.getTrailers().get(Metadata.Key.of("message", Metadata.ASCII_STRING_MARSHALLER));
            log.error(errorMessage);
        }
        return null;
    }
}
