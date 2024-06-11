package no.nav.tag.tiltaksgjennomforing.arena;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.dto.ArenaKafkaMessage;
import no.nav.tag.tiltaksgjennomforing.arena.dto.TiltakdeltakerEndretDto;
import no.nav.tag.tiltaksgjennomforing.arena.dto.TiltakgjennomforingEndretDto;
import no.nav.tag.tiltaksgjennomforing.arena.dto.TiltaksakEndretDto;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.kafka.Topics;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile(Miljø.LOCAL)
public class ArenaKafkaConsumer {
    private final ObjectMapper objectMapper;

    public ArenaKafkaConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = Topics.ARENA_TILTAKGJENNOMFORING_ENDRET)
    public void arenaTiltakgjennomforingEndret(String meldingStr) throws JsonProcessingException {
        TypeReference<ArenaKafkaMessage<TiltakgjennomforingEndretDto>> typeReference = new TypeReference<>() {};
        ArenaKafkaMessage<TiltakgjennomforingEndretDto> melding = objectMapper.readValue(meldingStr, typeReference);

        log.info("Mottatt melding for" + Topics.ARENA_TILTAKGJENNOMFORING_ENDRET + ": {}", melding);
    }

    @KafkaListener(topics = Topics.ARENA_TILTAKDELTAKER_ENDRET)
    public void arenaTiltakdeltakerEndret(String meldingStr) throws JsonProcessingException {
        TypeReference<ArenaKafkaMessage<TiltakdeltakerEndretDto>> typeReference = new TypeReference<>() {};
        ArenaKafkaMessage<TiltakdeltakerEndretDto> melding = objectMapper.readValue(meldingStr, typeReference);

        log.info("Mottatt melding for" + Topics.ARENA_TILTAKDELTAKER_ENDRET + ": {}", melding);
    }

    @KafkaListener(topics = Topics.ARENA_TILTAKSSAK_ENDRET)
    public void arenaTiltaksakEndret(String meldingStr) throws JsonProcessingException {
        TypeReference<ArenaKafkaMessage<TiltaksakEndretDto>> typeReference = new TypeReference<>() {};
        ArenaKafkaMessage<TiltaksakEndretDto> melding = objectMapper.readValue(meldingStr, typeReference);

        log.info("Mottatt melding for" + Topics.ARENA_TILTAKSSAK_ENDRET + ": {}", melding);
    }
}
