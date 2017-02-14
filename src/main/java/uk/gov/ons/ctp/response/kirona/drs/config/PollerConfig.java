package uk.gov.ons.ctp.response.kirona.drs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties("poller")
@Data
public class PollerConfig {
    private boolean fixedRate;

    private int maxMessagesPerPoll;
    private int queueLength;
    private int supportHoursBuffer;

    private long fixedDelay;
    private long initialDelay;

    private List<String> supportHoursStart;
    private List<String> supportHoursEnd;
}
