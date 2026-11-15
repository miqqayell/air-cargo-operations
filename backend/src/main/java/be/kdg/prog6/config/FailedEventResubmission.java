package be.kdg.prog6.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;

@Configuration
@EnableScheduling
public class FailedEventResubmission {

    private static final Logger log = LoggerFactory.getLogger(FailedEventResubmission.class);

    private final FailedEventPublications failedEventPublications;
    private final Duration minimumAge;

    public FailedEventResubmission(FailedEventPublications failedEventPublications,
                                   @Value("${events.resubmission.interval}") Duration minimumAge) {
        this.failedEventPublications = failedEventPublications;
        this.minimumAge = minimumAge;
    }

    @Scheduled(fixedDelayString = "${events.resubmission.interval}", initialDelayString = "${events.resubmission.interval}")
    void resubmitFailedPublications() {
        log.debug("Resubmitting failed event publications older than {}", minimumAge);
        failedEventPublications.resubmit(ResubmissionOptions.defaults().withMinAge(minimumAge));
    }
}
