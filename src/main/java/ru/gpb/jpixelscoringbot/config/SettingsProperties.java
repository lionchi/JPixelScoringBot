package ru.gpb.jpixelscoringbot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "settings")
public class SettingsProperties {

    private Set<String> adminNicknames;
    private Integer lifeTimeReportInMonths;
    private Integer numberOfQuestionsAboutJava;
}
