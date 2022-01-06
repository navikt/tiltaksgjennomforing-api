package no.nav.tag.tiltaksgjennomforing.varsel.kafka;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeStatus;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.RefusjonGodkjentMelding;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.Topics;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
@Slf4j
public class TilskuddsperiodeUtbetaltKafkaConsumer {
    private final AvtaleRepository avtaleRepository;
    private final ObjectMapper objectMapper;

    public TilskuddsperiodeUtbetaltKafkaConsumer(AvtaleRepository avtaleRepository, ObjectMapper objectMapper) {
        this.avtaleRepository = avtaleRepository;
        this.objectMapper = objectMapper;
    }

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

    public ConsumerFactory<String, RefusjonGodkjentMelding> refusjonConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, gcpBootstrapServers);
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
        props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "jks");
        props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslTruststoreLocationEnvKey);
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslTruststorePasswordEnvKey);
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, sslKeystoreLocationEnvKey);
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslKeystorePasswordEnvKey);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "tiltaksgjennomforing-api");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props,
            new StringDeserializer(),
            new JsonDeserializer<>(RefusjonGodkjentMelding.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RefusjonGodkjentMelding> refusjonContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, RefusjonGodkjentMelding>();
        factory.setConsumerFactory(refusjonConsumerFactory());
        return factory;
    }

    /*
     TODO: Bør flyttes til tilskuddsperiode pakken, men det er flere kafka configs som gjør at feiler.
     Det funker her.
     */
    @KafkaListener(topics = Topics.REFUSJON_GODKJENT, containerFactory = "refusjonContainerFactory")
    public void tilskuddsperiodeUtbetalt(String jsonMelding) throws JsonProcessingException {
        RefusjonGodkjentMelding melding = objectMapper.readValue(jsonMelding, RefusjonGodkjentMelding.class);
        Avtale avtale = avtaleRepository.findById(melding.getAvtaleId()).orElseThrow();
        avtale.getTilskuddPeriode().stream()
            .filter(it -> it.getId().equals(melding.getTilskuddsperiodeId()))
            .findFirst()
            .orElseThrow()
            .setStatus(TilskuddPeriodeStatus.UTBETALT);
        avtaleRepository.save(avtale);
    }
}
