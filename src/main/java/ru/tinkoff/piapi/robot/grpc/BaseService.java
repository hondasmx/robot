package ru.tinkoff.piapi.robot.grpc;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ApplicationProtocolConfig;
import io.grpc.netty.shaded.io.netty.handler.ssl.ApplicationProtocolNames;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.MetadataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import javax.net.ssl.SSLException;
import java.util.concurrent.TimeUnit;
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

    @Value("${grpc.url}")
    private String url;
    @Value("${grpc.port}")
    private String port;

    public ManagedChannel managedChannel() {
        return NettyChannelBuilder
                .forAddress(url, Integer.parseInt(port))
                .sslContext(createSslContext())
                .keepAliveTimeout(60, TimeUnit.SECONDS)
                .negotiationType(NegotiationType.TLS)
                .build();
    }

    private SslContext createSslContext() {
        try {
            SslContextBuilder grpcSslContexts = GrpcSslContexts.forClient();
            grpcSslContexts.trustManager(InsecureTrustManagerFactory.INSTANCE);
            return grpcSslContexts
                    .applicationProtocolConfig(
                            new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.NPN_AND_ALPN,
                                    ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                                    ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                                    ApplicationProtocolNames.HTTP_2))
                    .build();
        } catch (SSLException e) {
            log.error("Error in create SSL connection! {}", e.toString());
            return null;
        }
    }
}
