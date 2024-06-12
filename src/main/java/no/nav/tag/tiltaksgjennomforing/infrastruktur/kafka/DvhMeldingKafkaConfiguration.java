package no.nav.tag.tiltaksgjennomforing.infrastruktur.kafka;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.datavarehus.AvroTiltakHendelse;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
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
public class DvhMeldingKafkaConfiguration {

    @Value("${KAFKA_SCHEMA_REGISTRY:#{null}}}")
    private String schemaRegistryUrl;
    @Value("${KAFKA_SCHEMA_REGISTRY_USER:#{null}}}")
    private String schemaRegistryUser;
    @Value("${KAFKA_SCHEMA_REGISTRY_PASSWORD:#{null}}}")
    private String schemaRegistryPassword;

    @Bean
    public KafkaTemplate<String, AvroTiltakHendelse> dvhMeldingKafkaTemplate(KafkaProperties kafkaProperties) {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfigs(kafkaProperties)));
    }

    private Map<String, Object> producerConfigs(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildProducerProperties(null);

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("basic.auth.credentials.source", "USER_INFO");
        props.put("basic.auth.user.info", schemaRegistryUser + ":" + schemaRegistryPassword);

        return props;
    }
}
