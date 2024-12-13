package no.nav.tag.tiltaksgjennomforing.arena.job;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementAggregate;
import no.nav.tag.tiltaksgjennomforing.arena.service.ArenaAgreementService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Profile({ Miljø.DEV_FSS_Q0, Miljø.DEV_FSS, Miljø.PROD_FSS })
public class ArenaAgreementJob {
    private final ArenaAgreementService arenaAgreementService;

    public ArenaAgreementJob(ArenaAgreementService arenaAgreementService) {
        this.arenaAgreementService = arenaAgreementService;
    }

    @Scheduled(fixedRate =  30, timeUnit = TimeUnit.SECONDS)
    public void run() {
        List<ArenaAgreementAggregate> arenaAgreements = arenaAgreementService.getArenaAgreementsForProcessing();

        if (!arenaAgreements.isEmpty()) {
            arenaAgreementService.processAgreements(arenaAgreements);
        }
    }

}
