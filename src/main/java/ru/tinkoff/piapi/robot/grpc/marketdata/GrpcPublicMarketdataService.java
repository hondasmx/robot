package ru.tinkoff.piapi.robot.grpc.marketdata;

import io.grpc.ManagedChannel;
import io.grpc.stub.MetadataUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.GetLastPricesRequest;
import ru.tinkoff.piapi.contract.v1.GetTradingStatusRequest;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.robot.grpc.BaseService;
import ru.tinkoff.piapi.robot.utils.Helpers;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrpcPublicMarketdataService extends BaseService<MarketDataServiceGrpc.MarketDataServiceBlockingStub> {

    private ManagedChannel managedChannel;
    private final Helpers helpers;

    @Override
    protected MarketDataServiceGrpc.MarketDataServiceBlockingStub getStub() {
        managedChannel = managedChannel();
        return MarketDataServiceGrpc
                .newBlockingStub(managedChannel)
                .withInterceptors(MetadataUtils.newCaptureMetadataInterceptor(headersCapture, trailersCapture));
    }

    public SecurityTradingStatus getTradingStatus(String figi) {
        var request = GetTradingStatusRequest.newBuilder().setFigi(figi).build();
        var body = helpers.unaryCall(() -> getStubWithHeaders().getTradingStatus(request));
        managedChannel.shutdownNow();
        return getResponse(body).getResponse().getTradingStatus();
    }

    public List<LastPrice> getLastPrices() {
        var request = GetLastPricesRequest.newBuilder().build();
        var body = helpers.unaryCall(() -> getStubWithHeaders().getLastPrices(request));
        managedChannel.shutdownNow();
        return getResponse(body).getResponse().getLastPricesList();
    }
}
