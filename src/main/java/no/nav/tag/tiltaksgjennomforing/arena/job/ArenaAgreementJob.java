package no.nav.tag.tiltaksgjennomforing.arena.job;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaAgreementAggregate;
import no.nav.tag.tiltaksgjennomforing.arena.service.ArenaAgreementService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile({ Miljø.DEV_FSS, Miljø.PROD_FSS })
public class ArenaAgreementJob {
    private static final int ONE_MIN_IN_MS = 60 * 1000;

    private final ArenaAgreementService arenaAgreementService;

    public ArenaAgreementJob(ArenaAgreementService arenaAgreementService) {
        this.arenaAgreementService = arenaAgreementService;
    }

    @Scheduled(fixedDelay = ONE_MIN_IN_MS)
    public void run() {
        List<ArenaAgreementAggregate> arenaAgreements = arenaAgreementService.getArenaAgreementsForProcessing();

        if (!arenaAgreements.isEmpty()) {
            arenaAgreementService.processAgreements(arenaAgreements);
        }
    }

}
