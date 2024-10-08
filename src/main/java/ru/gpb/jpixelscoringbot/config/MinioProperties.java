package ru.gpb.jpixelscoringbot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String host;
    private String accessToken;
    private String secretToken;
    private String nameBucket;
}
