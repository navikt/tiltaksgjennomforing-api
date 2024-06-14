package no.nav.tag.tiltaksgjennomforing.arena.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.service.ArenaProcessingService;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.kafka.Topics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile(Miljø.LOCAL)
public class ArenaKafkaConsumer {
    private final ArenaProcessingService arenaProcessingService;

    public ArenaKafkaConsumer(ArenaProcessingService arenaProcessingService) {
        this.arenaProcessingService = arenaProcessingService;
    }

    @KafkaListener(topics = Topics.ARENA_TILTAKGJENNOMFORING_ENDRET)
    public void arenaTiltakgjennomforingEndret(ConsumerRecord<String, String> record) {
        log.info("Mottatt melding for" + Topics.ARENA_TILTAKGJENNOMFORING_ENDRET + ": {}", record.key());
        arenaProcessingService.process(record.key(), record.value());
    }

    @KafkaListener(topics = Topics.ARENA_TILTAKDELTAKER_ENDRET)
    public void arenaTiltakdeltakerEndret(ConsumerRecord<String, String> record) {
        log.info("Mottatt melding for" + Topics.ARENA_TILTAKDELTAKER_ENDRET + ": {}", record.key());
        arenaProcessingService.process(record.key(), record.value());
    }

    @KafkaListener(topics = Topics.ARENA_TILTAKSSAK_ENDRET)
    public void arenaTiltaksakEndret(ConsumerRecord<String, String> record) {
        log.info("Mottatt melding for" + Topics.ARENA_TILTAKSSAK_ENDRET + ": {}", record.key());
        arenaProcessingService.process(record.key(), record.value());
    }
}
