package no.nav.tag.tiltaksgjennomforing.arena.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.configuration.ArenaKafkaProperties;
import no.nav.tag.tiltaksgjennomforing.arena.service.ArenaEventProcessingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile(Miljø.LOCAL)
public class ArenaKafkaConsumer {
    private final ArenaKafkaProperties arenaKafkaProperties;
    private final ArenaEventProcessingService arenaEventProcessingService;

    public ArenaKafkaConsumer(
        ArenaEventProcessingService arenaEventProcessingService,
        ArenaKafkaProperties arenaKafkaProperties
    ) {
        this.arenaEventProcessingService = arenaEventProcessingService;
        this.arenaKafkaProperties = arenaKafkaProperties;
    }

    @KafkaListener(topics = "${tiltaksgjennomforing.arena.kafka.tiltakgjennomforing-endret-topic}")
    public void arenaTiltakgjennomforingEndret(ConsumerRecord<String, String> record) {
        log.info("Mottatt melding for {}: {}", arenaKafkaProperties.getTiltakgjennomforingEndretTopic(), record.key());
        arenaEventProcessingService.process(record.key(), record.value());
    }

    @KafkaListener(topics = "${tiltaksgjennomforing.arena.kafka.tiltakdeltaker-endret-topic}")
    public void arenaTiltakdeltakerEndret(ConsumerRecord<String, String> record) {
        log.info("Mottatt melding for {}: {}", arenaKafkaProperties.getTiltakdeltakerEndretTopic(), record.key());
        arenaEventProcessingService.process(record.key(), record.value());
    }

    @KafkaListener(topics = "${tiltaksgjennomforing.arena.kafka.tiltakssak-endret-topic}")
    public void arenaTiltaksakEndret(ConsumerRecord<String, String> record) {
        log.info("Mottatt melding for {}: {}", arenaKafkaProperties.getTiltakssakEndretTopic(), record.key());
        arenaEventProcessingService.process(record.key(), record.value());
    }
}
