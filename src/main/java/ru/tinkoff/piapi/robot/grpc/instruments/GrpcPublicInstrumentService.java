package ru.tinkoff.piapi.robot.grpc.instruments;

import io.grpc.ManagedChannel;
import io.grpc.stub.MetadataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.robot.grpc.BaseService;

import java.util.List;

@Slf4j
@Service
public class GrpcPublicInstrumentService extends BaseService<InstrumentsServiceGrpc.InstrumentsServiceBlockingStub> {

    private final ManagedChannel managedChannel;

    public GrpcPublicInstrumentService(@Qualifier("instruments") ManagedChannel managedChannel) {
        this.managedChannel = managedChannel;
    }

    @Override
    protected InstrumentsServiceGrpc.InstrumentsServiceBlockingStub getStub() {
        return InstrumentsServiceGrpc
                .newBlockingStub(managedChannel)
                .withInterceptors(MetadataUtils.newCaptureMetadataInterceptor(headersCapture, trailersCapture));
    }

    public List<Etf> getEtfs(InstrumentStatus instrumentStatus) {
        log.info("get etfs started. status {}", instrumentStatus.name());
        var request = InstrumentsRequest.newBuilder().setInstrumentStatus(instrumentStatus).build();
        var body = getStubWithHeaders().etfs(request);
        log.info("get etfs completed");
        return getResponse(body).getResponse().getInstrumentsList();
    }

    public List<Share> getShares(InstrumentStatus instrumentStatus) {
        log.info("get shares started. status {}", instrumentStatus.name());
        var request = InstrumentsRequest.newBuilder().setInstrumentStatus(instrumentStatus).build();
        var body = getStubWithHeaders().shares(request);
        log.info("get shares completed");
        return getResponse(body).getResponse().getInstrumentsList();
    }

    public List<Currency> getCurrencies(InstrumentStatus instrumentStatus) {
        log.info("get currencies started. status {}", instrumentStatus.name());
        var request = InstrumentsRequest.newBuilder().setInstrumentStatus(instrumentStatus).build();
        var body = getStubWithHeaders().currencies(request);
        log.info("get currencies completed");
        return getResponse(body).getResponse().getInstrumentsList();
    }

    public List<Future> getFutures(InstrumentStatus instrumentStatus) {
        log.info("get futures started. status {}", instrumentStatus.name());
        var request = InstrumentsRequest.newBuilder().setInstrumentStatus(instrumentStatus).build();
        var body = getStubWithHeaders().futures(request);
        log.info("get futures completed");
        return getResponse(body).getResponse().getInstrumentsList();
    }

    public List<Bond> getBonds(InstrumentStatus instrumentStatus) {
        log.info("get bonds started. status {}", instrumentStatus.name());
        var request = InstrumentsRequest.newBuilder().setInstrumentStatus(instrumentStatus).build();
        var body = getStubWithHeaders().bonds(request);
        log.info("get bonds completed");
        return getResponse(body).getResponse().getInstrumentsList();
    }
}
