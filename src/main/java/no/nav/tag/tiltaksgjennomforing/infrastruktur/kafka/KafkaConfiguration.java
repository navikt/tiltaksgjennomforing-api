package no.nav.tag.tiltaksgjennomforing.infrastruktur.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.datavarehus.AvroTiltakHendelse;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.RefusjonEndretStatusMelding;
import no.nav.tag.tiltaksgjennomforing.varsel.Sms;
import no.nav.tag.tiltaksgjennomforing.varsel.kafka.RefusjonVarselMelding;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Configuration
@Slf4j
@EnableKafka
public class KafkaConfiguration {

    private static final String JAVA_KEYSTORE  = "jks";
    private final String PKCS12 = "PKCS12";
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
    @Value("${no.nav.gcp.kafka.aiven.security-protocol}")
    private String securityProtocol;
    @Value("${no.nav.gcp.kafka.aiven.schema-registry-url}")
    private String schemaRegistryUrl;
    @Value("${no.nav.gcp.kafka.aiven.schema-registry-credentials-source}")
    private String schemaRegistryCredentialsSource;
    @Value("${no.nav.gcp.kafka.aiven.schema-registry-user-info}")
    private String schemaRegistryUserInfo;
    
    private Map<String, Object> producerConfigs() {
        Map<String, Object> config = new HashMap<>();
        config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, gcpBootstrapServers);
        
        config.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
        config.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, sslKeystoreLocationEnvKey);
        config.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslKeystorePasswordEnvKey);
        config.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, PKCS12);
        config.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslTruststoreLocationEnvKey);
        config.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslTruststorePasswordEnvKey);
        config.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, JAVA_KEYSTORE);
        
        return config;
    }
    
    private Map<String, Object> consumerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, gcpBootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "tiltaksgjennomforing-api");
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        
        config.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
        config.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, sslKeystoreLocationEnvKey);
        config.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslKeystorePasswordEnvKey);
        config.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, PKCS12);
        config.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslTruststoreLocationEnvKey);
        config.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslTruststorePasswordEnvKey);
        config.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, JAVA_KEYSTORE);

        return config;
    }

    private <T>ProducerFactory<String, T> getProducerFactory(Serializer<T> valueSerializer) {
        return getProducerFactory(valueSerializer, producerConfigs());
    }
    
    private <T>ProducerFactory<String, T> getProducerFactory(Serializer<T> valueSerializer, Map<String, Object> config) {
        return new DefaultKafkaProducerFactory<>(
            config,
            new StringSerializer(),
            valueSerializer
        );
    }
    
    private <T>ConsumerFactory<String, T> getConsumerFactory(Deserializer<T> valueDeserializer) {
        return new DefaultKafkaConsumerFactory<>(
                consumerConfig(),
                new StringDeserializer(),
                valueDeserializer
        );
    }
    
    @Bean
    public KafkaTemplate<String, AvroTiltakHendelse> dvhMeldingKafkaTemplate() {
        Map<String, Object> config = producerConfigs();
        config.put("schema.registry.url", schemaRegistryUrl);
        config.put("basic.auth.credentials.source", schemaRegistryCredentialsSource);
        config.put("basic.auth.user.info", schemaRegistryUserInfo);

        return new KafkaTemplate<>(getProducerFactory(new GenericKafkaAvroSerializer<>(), config));
    }

    @Bean
    public KafkaTemplate<String, String> auditEntryKafkaTemplate() {
        return new KafkaTemplate<>(getProducerFactory(new StringSerializer()));
    }
    
    @Bean
    public KafkaTemplate<String, Sms> tiltaksgjennomforingVarselKafkaTemplate() {
        return new KafkaTemplate<>(getProducerFactory(new JsonSerializer<>()));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RefusjonVarselMelding> varselContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, RefusjonVarselMelding>();
        factory.setConsumerFactory(
            getConsumerFactory(new JsonDeserializer<>(RefusjonVarselMelding.class, false))
        );
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RefusjonEndretStatusMelding> refusjonEndretStatusContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, RefusjonEndretStatusMelding>();
        factory.setConsumerFactory(
            getConsumerFactory(new JsonDeserializer<>(RefusjonEndretStatusMelding.class, false))
        );
        return factory;
    }

}
