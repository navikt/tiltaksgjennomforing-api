package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Configuration
@EnableKafka
public class KafkaConfiguration {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapAddress;

  @Bean
  public KafkaTemplate<String, Statistikkformidlingsmelding> kafkaTemplateStatistikkformidlingsmelding() {
    return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(getProducerConfigs()));
  }

  @Bean
  public KafkaTemplate<String, SmsVarselMelding> kafkaTemplateSmsVarselMelding() {
    return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(getProducerConfigs()));
  }

  private Map<String, Object> getProducerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return props;
  }
}
