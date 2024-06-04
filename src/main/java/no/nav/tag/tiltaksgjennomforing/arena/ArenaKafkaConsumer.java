package no.nav.tag.tiltaksgjennomforing.arena;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.dto.ArenaKafkaMessage;
import no.nav.tag.tiltaksgjennomforing.arena.dto.TiltakgjennomforingEndretDto;
import no.nav.tag.tiltaksgjennomforing.arena.dto.TiltaksakEndretDto;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.kafka.Topics;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({ Miljø.LOCAL })
public class ArenaKafkaConsumer {

    public ArenaKafkaConsumer() {
        log.info("Starter ArenaKafkaConsumer");
    }

    @KafkaListener(topics = Topics.ARENA_TILTAKGJENNOMFORING_ENDRET, containerFactory = "arenaTiltakgjennomforingEndretContainerFactory")
    public void arenaTiltakgjennomforingEndret(ArenaKafkaMessage<TiltakgjennomforingEndretDto> melding) {
        log.info("Mottatt melding for" + Topics.ARENA_TILTAKGJENNOMFORING_ENDRET + ": {}", melding);
    }

    @KafkaListener(topics = Topics.ARENA_TILTAKDELTAKER_ENDRET, containerFactory = "arenaTiltakdeltakerEndretContainerFactory")
    public void arenaTiltakdeltakerEndret(ArenaKafkaMessage<TiltaksakEndretDto> melding) {
        log.info("Mottatt melding for" + Topics.ARENA_TILTAKDELTAKER_ENDRET + ": {}", melding);
    }

    @KafkaListener(topics = Topics.ARENA_TILTAKSSAK_ENDRET, containerFactory = "arenaTiltaksakEndretContainerFactory")
    public void arenaTiltaksakEndret(ArenaKafkaMessage<TiltaksakEndretDto> melding) {
        log.info("Mottatt melding for" + Topics.ARENA_TILTAKSSAK_ENDRET + ": {}", melding);
    }
}
