package no.nav.tag.tiltaksgjennomforing.varsel.kafka.avien;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Configuration
@Slf4j
@EnableKafka
public class AivenKafkaConfiguration {

  @Value("${no.nav.gcp.kafka.aiven.bootstrap-servers}")
  private String bootstrapServers;

  @Bean
  Map<String, Object> producerConfigs() {
    Map<String, Object> props = new HashMap<>();
  //  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    return props;
  }
/*
  @Bean
  ProducerFactory<String, String> producerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean
  KafkaTemplate<String, String> kafkaTemplate() {
    KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>(producerFactory());
    kafkaTemplate.setMessageConverter(new StringJsonMessageConverter());
    return kafkaTemplate;
  }*/
}
