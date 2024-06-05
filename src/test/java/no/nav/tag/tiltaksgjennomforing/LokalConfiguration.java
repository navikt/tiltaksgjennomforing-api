package no.nav.tag.tiltaksgjennomforing;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.dto.ArenaKafkaMessage;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.kafka.Topics;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.NotifikasjonService;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;

import java.util.Map;

@Slf4j
@Configuration
public class LokalConfiguration {

  @Bean
  NotifikasjonService notifikasjon() { return Mockito.mock(NotifikasjonService.class);}

  @Bean
  @Profile(Miljø.IKKE_TEST)
  public EmbeddedKafkaZKBroker lokalKafkaBroker() {
    log.info("Starter lokal Kafka");

    return new EmbeddedKafkaZKBroker(
        1,
        true,
        Topics.ARENA_TILTAKGJENNOMFORING_ENDRET,
        Topics.ARENA_TILTAKDELTAKER_ENDRET,
        Topics.ARENA_TILTAKSSAK_ENDRET
    ).kafkaPorts(3333);
  }

  @Bean
  @Profile(Miljø.IKKE_TEST)
  public KafkaTemplate<String, ArenaKafkaMessage<?>> arenaMockKafkaTemplate(
      EmbeddedKafkaZKBroker lokalKafkaBroker
  ) {
    Map<String, Object> props = Map.of(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, lokalKafkaBroker.getBrokersAsString(),
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
    );

    return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
  }
}
