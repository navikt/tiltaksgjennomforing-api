package no.nav.tag.tiltaksgjennomforing.infrastruktur.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Configuration
@Slf4j
@EnableKafka
public class DefaultKafkaConfiguration {

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildProducerProperties(null);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

}
