package ru.tinkoff.piapi.robot.grpc;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ApplicationProtocolConfig;
import io.grpc.netty.shaded.io.netty.handler.ssl.ApplicationProtocolNames;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.net.ssl.SSLException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GrpcConfiguration {

    @Value("${grpc.url}")
    private String url;
    @Value("${grpc.port}")
    private String port;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ManagedChannel managedChannel() {
        log.info("creating new managed channel");
        return NettyChannelBuilder
                .forAddress(url, Integer.parseInt(port))
                .sslContext(createSslContext())
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
