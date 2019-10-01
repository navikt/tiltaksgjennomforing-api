package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;

@Profile("kafka")
@Configuration
@EnableKafka
public class KafkaConfiguration {
}
