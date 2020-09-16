package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Configuration
@EnableKafka
public class KafkaConfiguration {
}
