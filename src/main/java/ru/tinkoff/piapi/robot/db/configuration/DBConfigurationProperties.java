package ru.tinkoff.piapi.robot.db.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.datasource")
public class DBConfigurationProperties {
    private String username;
    private String password;
    private String database;
    private Integer port;
    private String serverName;
}
