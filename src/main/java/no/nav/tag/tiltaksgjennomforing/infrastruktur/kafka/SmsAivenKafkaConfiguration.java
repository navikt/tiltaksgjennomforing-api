package no.nav.tag.tiltaksgjennomforing.infrastruktur.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.varsel.Sms;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Configuration
@Slf4j
@EnableKafka
public class SmsAivenKafkaConfiguration {

    @Value("${KAFKA_SCHEMA_REGISTRY:#{null}}}")
    private String schemaRegistryUrl;
    @Value("${KAFKA_SCHEMA_REGISTRY_USER:#{null}}}:${KAFKA_SCHEMA_REGISTRY_PASSWORD:#{null}}}")
    private String schemaRegistryUserInfo;

    @Bean
    public KafkaTemplate<String, Sms> aivenTiltaksgjennomforingVarsel(KafkaProperties kafkaProperties) {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfigs(kafkaProperties)));
    }

    private Map<String, Object> producerConfigs(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildProducerProperties(null);

        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("basic.auth.credentials.source", "USER_INFO");
        props.put("basic.auth.user.info", schemaRegistryUserInfo);

        return props;
    }
}
