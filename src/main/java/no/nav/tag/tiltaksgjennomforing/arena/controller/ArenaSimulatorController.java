package no.nav.tag.tiltaksgjennomforing.arena.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.configuration.ArenaKafkaProperties;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaKafkaMessage;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakdeltaker;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakgjennomforing;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakssak;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Operation;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementAggregate;
import no.nav.tag.tiltaksgjennomforing.arena.service.ArenaAgreementService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Unprotected
@RestController
@Profile(Miljø.LOCAL)
@RequestMapping("/arena/simulator")
public class ArenaSimulatorController {
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, ArenaKafkaMessage> arenaMockKafkaTemplate;
    private final ArenaKafkaProperties arenaKafkaProperties;
    private final ArenaAgreementService arenaAgreementService;

    public ArenaSimulatorController(
            ObjectMapper objectMapper,
            KafkaTemplate<String, ArenaKafkaMessage> arenaMockKafkaTemplate,
            ArenaKafkaProperties arenaKafkaProperties,
            ArenaAgreementService arenaAgreementService
    ) {
        this.objectMapper = objectMapper;
        this.arenaMockKafkaTemplate = arenaMockKafkaTemplate;
        this.arenaKafkaProperties = arenaKafkaProperties;
        this.arenaAgreementService = arenaAgreementService;
    }

    @PostMapping("/tiltakgjennomforing-endret")
    public ResponseEntity<?> tiltakgjennomforingEndret(
        @RequestBody ArenaKafkaMessage melding
    ) {
        try {
            JsonNode payload = Operation.DELETE.getOperation().equals(melding.opType()) ? melding.before() : melding.after();
            ArenaTiltakgjennomforing tiltakgjennomforingEndret =  objectMapper.treeToValue(payload, ArenaTiltakgjennomforing.class);

            String id = tiltakgjennomforingEndret.getTiltakgjennomforingId().toString();
            arenaMockKafkaTemplate.send(arenaKafkaProperties.getTiltakgjennomforingEndretTopic(), id, melding);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getCause().getMessage());
        }
    }

    @PostMapping("/tiltakssak-endret")
    public ResponseEntity<?> tiltakssakEndret(
        @RequestBody ArenaKafkaMessage melding
    ) {
        try {
            JsonNode payload = Operation.DELETE.getOperation().equals(melding.opType()) ? melding.before() : melding.after();
            ArenaTiltakssak tiltaksakEndret =  objectMapper.treeToValue(payload, ArenaTiltakssak.class);

            String id = tiltaksakEndret.getSakId().toString();
            arenaMockKafkaTemplate.send(arenaKafkaProperties.getTiltakssakEndretTopic(), id, melding);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getCause().getMessage());
        }
    }

    @PostMapping("/tiltakdeltaker-endret")
    public ResponseEntity<?> tiltakdeltakerEndret(
        @RequestBody ArenaKafkaMessage melding
    ) {
        try {
            JsonNode payload = Operation.DELETE.getOperation().equals(melding.opType()) ? melding.before() : melding.after();
            ArenaTiltakdeltaker tiltakdeltakerEndret =  objectMapper.treeToValue(payload, ArenaTiltakdeltaker.class);

            String id = tiltakdeltakerEndret.getTiltakdeltakerId().toString();
            arenaMockKafkaTemplate.send(arenaKafkaProperties.getTiltakdeltakerEndretTopic(), id, melding);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getCause().getMessage());
        }
    }

    @GetMapping("/trigger")
    public ResponseEntity<?> trigger() {
        List<ArenaAgreementAggregate> arenaAgreements = arenaAgreementService.getArenaAgreementsForProcessing();

        if (!arenaAgreements.isEmpty()) {
            arenaAgreementService.processAgreements(arenaAgreements);
        }

        return ResponseEntity.noContent().build();
    }
}
