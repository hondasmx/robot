package ru.tinkoff.piapi.robot.grpc;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "config")
@Data
public class StreamConfiguration {

    private Config candles;
    private Config orderbook;
    private Config trades;

    @Data
    public static class Config {
        private boolean dbWriteEnabled;
        private int streamCount;
    }
}
