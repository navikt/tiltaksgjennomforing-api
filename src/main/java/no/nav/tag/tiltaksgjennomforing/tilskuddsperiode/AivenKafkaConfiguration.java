package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;

import kafka.tools.ConsoleConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Configuration
@Slf4j
@EnableKafka
public class AivenKafkaConfiguration {

    private final String javaKeystore = "jks";
    private final String pkcs12 = "PKCS12";
    @Value("${no.nav.gcp.kafka.aiven.bootstrap-servers}")
    private String gcpBootstrapServers;
    @Value("${no.nav.gcp.kafka.aiven.truststore-path}")
    private String sslTruststoreLocationEnvKey;
    @Value("${no.nav.gcp.kafka.aiven.truststore-password}")
    private String sslTruststorePasswordEnvKey;
    @Value("${no.nav.gcp.kafka.aiven.keystore-path}")
    private String sslKeystoreLocationEnvKey;
    @Value("${no.nav.gcp.kafka.aiven.keystore-password}")
    private String sslKeystorePasswordEnvKey;

    private Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, gcpBootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
        props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, javaKeystore);
        props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, pkcs12);
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslTruststoreLocationEnvKey);
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslTruststorePasswordEnvKey);
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, sslKeystoreLocationEnvKey);
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslKeystorePasswordEnvKey);
        return props;
    }

    @Bean
    public KafkaTemplate<String, String> aivenKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfigs()));
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, Object> kafkaConsumerTemplate() {
        KafkaProperties kafkaProperties = new KafkaProperties();
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<String, Object>(props);
    }



    @Bean
    public <T> ConcurrentKafkaListenerContainerFactory<String, T> kafkaListenerContainerFactory(
            Class<T> containerType, KafkaProperties kafkaProperties, String groupId) {

        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        props.putAll(consumerConfig(groupId));
        DefaultKafkaConsumerFactory<String, T> defaultKafkaConsumerFactory = new DefaultKafkaConsumerFactory<>(
                props, new StringDeserializer(), valueDeserializer(containerType));
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(defaultKafkaConsumerFactory);

        return factory;
    }
    private <T> ErrorHandlingDeserializer<T> valueDeserializer(Class<T> targetType) {
        return new ErrorHandlingDeserializer<>(new JsonDeserializer<>(targetType, false));
    }
    private Map<String, Object> consumerConfig(String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 15000);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);

        return props;
    }

}
