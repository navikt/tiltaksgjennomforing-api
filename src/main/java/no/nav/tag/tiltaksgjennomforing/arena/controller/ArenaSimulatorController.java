package no.nav.tag.tiltaksgjennomforing.arena.controller;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementAggregate;
import no.nav.tag.tiltaksgjennomforing.arena.service.ArenaAgreementService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Unprotected
@RestController
@Profile(Miljø.LOCAL)
@RequestMapping("/arena/simulator")
public class ArenaSimulatorController {
    private final ArenaAgreementService arenaAgreementService;

    public ArenaSimulatorController(ArenaAgreementService arenaAgreementService) {
        this.arenaAgreementService = arenaAgreementService;
    }

    @GetMapping("/trigger")
    public ResponseEntity<?> trigger() {
        Map<UUID, ArenaAgreementAggregate> arenaAgreements = arenaAgreementService.getArenaAgreementsForProcessing();

        if (!arenaAgreements.isEmpty()) {
            arenaAgreementService.processAgreements(arenaAgreements);
        }

        return ResponseEntity.noContent().build();
    }
}
