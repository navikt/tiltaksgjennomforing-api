package no.nav.tag.tiltaksgjennomforing.arena.config;

import com.fasterxml.jackson.core.type.TypeReference;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.dto.ArenaKafkaMessage;
import no.nav.tag.tiltaksgjennomforing.arena.dto.TiltakdeltakerEndretDto;
import no.nav.tag.tiltaksgjennomforing.arena.dto.TiltakgjennomforingEndretDto;
import no.nav.tag.tiltaksgjennomforing.arena.dto.TiltaksakEndretDto;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@EnableKafka
@Configuration
@Profile(Miljø.LOCAL + "&" + Miljø.IKKE_TEST)
public class ArenaKafkaConsumerConfig {

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

    public <T>ConsumerFactory<String, ArenaKafkaMessage<T>> consumerFactory(TypeReference<ArenaKafkaMessage<T>> typeReference) {
        Map<String, Object> props = Map.ofEntries(
            Map.entry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, gcpBootstrapServers),
            Map.entry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol),
            Map.entry(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, ""),
            Map.entry(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "jks"),
            Map.entry(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12"),
            Map.entry(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslTruststoreLocationEnvKey),
            Map.entry(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslTruststorePasswordEnvKey),
            Map.entry(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, sslKeystoreLocationEnvKey),
            Map.entry(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslKeystorePasswordEnvKey),
            Map.entry(ConsumerConfig.GROUP_ID_CONFIG, "tiltaksgjennomforing-api"),
            Map.entry(JsonDeserializer.TRUSTED_PACKAGES, "*")
        );

        return new DefaultKafkaConsumerFactory<>(
            props,
            new StringDeserializer(),
            new JsonDeserializer<>(typeReference)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ArenaKafkaMessage<TiltakgjennomforingEndretDto>> arenaTiltakgjennomforingEndretContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, ArenaKafkaMessage<TiltakgjennomforingEndretDto>>();
        TypeReference<ArenaKafkaMessage<TiltakgjennomforingEndretDto>> typeReference = new TypeReference<>() {};
        factory.setConsumerFactory(consumerFactory(typeReference));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ArenaKafkaMessage<TiltakdeltakerEndretDto>> arenaTiltakdeltakerEndretContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, ArenaKafkaMessage<TiltakdeltakerEndretDto>>();
        TypeReference<ArenaKafkaMessage<TiltakdeltakerEndretDto>> typeReference = new TypeReference<>() {};
        factory.setConsumerFactory(consumerFactory(typeReference));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ArenaKafkaMessage<TiltaksakEndretDto>> arenaTiltaksakEndretContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, ArenaKafkaMessage<TiltaksakEndretDto>>();
        TypeReference<ArenaKafkaMessage<TiltaksakEndretDto>> typeReference = new TypeReference<>() {};
        factory.setConsumerFactory(consumerFactory(typeReference));
        return factory;
    }
}
