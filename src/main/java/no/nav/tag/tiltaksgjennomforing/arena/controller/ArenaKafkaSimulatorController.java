package no.nav.tag.tiltaksgjennomforing.arena.controller;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.dto.ArenaKafkaMessage;
import no.nav.tag.tiltaksgjennomforing.arena.dto.TiltakdeltakerEndretDto;
import no.nav.tag.tiltaksgjennomforing.arena.dto.TiltakgjennomforingEndretDto;
import no.nav.tag.tiltaksgjennomforing.arena.dto.TiltaksakEndretDto;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.kafka.Topics;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Unprotected
@RestController
@Profile({ Miljø.LOCAL })
@RequestMapping("/arena/simulator")
public class ArenaKafkaSimulatorController {
    private final KafkaTemplate<String, ArenaKafkaMessage<?>> arenaMockKafkaTemplate;

    public ArenaKafkaSimulatorController(KafkaTemplate<String, ArenaKafkaMessage<?>> arenaMockKafkaTemplate) {
        this.arenaMockKafkaTemplate = arenaMockKafkaTemplate;
    }

    @PostMapping("/tiltakgjennomforing-endret")
    public ResponseEntity<?> tiltakgjennomforingEndret(
        @RequestBody ArenaKafkaMessage<TiltakgjennomforingEndretDto> melding
    ) {
        try {
            String id = melding.after().tiltakgjennomforingId().toString();
            arenaMockKafkaTemplate.send(Topics.ARENA_TILTAKGJENNOMFORING_ENDRET, id, melding);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getCause().getMessage());
        }
    }

    @PostMapping("/tiltakssak-endret")
    public ResponseEntity<?> tiltakssakEndret(
        @RequestBody ArenaKafkaMessage<TiltaksakEndretDto> melding
    ) {
        try {
            String id = melding.after().sakId().toString();
            arenaMockKafkaTemplate.send(Topics.ARENA_TILTAKSSAK_ENDRET, id, melding);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getCause().getMessage());
        }
    }

    @PostMapping("/tiltakdeltaker-endret")
    public ResponseEntity<?> tiltakdeltakerEndret(
        @RequestBody ArenaKafkaMessage<TiltakdeltakerEndretDto> melding
    ) {
        try {
            String id = melding.after().tiltakdeltakerId().toString();
            arenaMockKafkaTemplate.send(Topics.ARENA_TILTAKDELTAKER_ENDRET, id, melding);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getCause().getMessage());
        }
    }
}
