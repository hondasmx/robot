package ru.tinkoff.piapi.robot.grpc;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.Metadata;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.MetadataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.atomic.AtomicReference;


@Slf4j
public abstract class BaseService<T extends AbstractStub<T>> {

    protected final AtomicReference<Metadata> trailersCapture = new AtomicReference<>();
    protected final AtomicReference<Metadata> headersCapture = new AtomicReference<>();

    @Value("${auth.token}")
    private String token;

    protected abstract T getStub();

    protected T getStubWithHeaders() {
        var stub = getStub();
        var headers = new Metadata();
        var authToken = "Bearer " + token;
        headers.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), authToken);
        return stub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }


    protected <V extends GeneratedMessageV3> CapturedResponse<V> getResponse(V body) {
        return new CapturedResponse<V>()
                .setResponse(body)
                .setHeaders(headersCapture.get())
                .setTrailers(trailersCapture.get());
    }
}
