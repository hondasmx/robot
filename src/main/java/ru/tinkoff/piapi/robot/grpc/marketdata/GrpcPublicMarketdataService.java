package ru.tinkoff.piapi.robot.grpc.marketdata;

import io.grpc.ManagedChannel;
import io.grpc.stub.MetadataUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.GetTradingStatusRequest;
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.robot.grpc.BaseService;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrpcPublicMarketdataService extends BaseService<MarketDataServiceGrpc.MarketDataServiceBlockingStub> {

    private final ManagedChannel managedChannel;


    @Override
    protected MarketDataServiceGrpc.MarketDataServiceBlockingStub getStub() {
        return MarketDataServiceGrpc
                .newBlockingStub(managedChannel)
                .withInterceptors(MetadataUtils.newCaptureMetadataInterceptor(headersCapture, trailersCapture));
    }

    public SecurityTradingStatus getTradingStatus(String figi) {
        var request = GetTradingStatusRequest.newBuilder().setFigi(figi).build();
        var body = getStubWithHeaders().getTradingStatus(request);
        return getResponse(body).getResponse().getTradingStatus();
    }
}
