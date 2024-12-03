package no.nav.tag.tiltaksgjennomforing.arena.job;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementAggregate;
import no.nav.tag.tiltaksgjennomforing.arena.service.ArenaAgreementService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.leader.LeaderPodCheck;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Profile({ Miljø.DEV_FSS, Miljø.PROD_FSS })
public class ArenaAgreementJob {
    private final ArenaAgreementService arenaAgreementService;
    private final FeatureToggleService featureToggleService;
    private final LeaderPodCheck leaderPodCheck;

    public ArenaAgreementJob(
            ArenaAgreementService arenaAgreementService,
            FeatureToggleService featureToggleService,
            LeaderPodCheck leaderPodCheck
    ) {
        this.arenaAgreementService = arenaAgreementService;
        this.featureToggleService = featureToggleService;
        this.leaderPodCheck = leaderPodCheck;
    }

    @Scheduled(fixedRate =  30, timeUnit = TimeUnit.SECONDS)
    public void run() {
        if (!leaderPodCheck.isLeaderPod() || !featureToggleService.isEnabled(FeatureToggle.ARENA_AVTALE_JOBB)) {
            return;
        }

        List<ArenaAgreementAggregate> arenaAgreements = arenaAgreementService.getArenaAgreementsForProcessing();

        if (!arenaAgreements.isEmpty()) {
            arenaAgreementService.processAgreements(arenaAgreements);
        }
    }

}
