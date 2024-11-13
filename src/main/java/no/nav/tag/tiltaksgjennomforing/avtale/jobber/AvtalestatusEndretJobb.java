package no.nav.tag.tiltaksgjennomforing.avtale.jobber;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.leader.LeaderPodCheck;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({ Miljø.DEV_GCP_LABS, Miljø.DEV_FSS, Miljø.PROD_FSS })
public class AvtalestatusEndretJobb {

    private final AvtaleRepository avtaleRepository;
    private final LeaderPodCheck leaderPodCheck;

    public AvtalestatusEndretJobb(
        AvtaleRepository avtaleRepository,
        LeaderPodCheck leaderPodCheck
    ) {
        this.avtaleRepository = avtaleRepository;
        this.leaderPodCheck = leaderPodCheck;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void run() {
        if (!leaderPodCheck.isLeaderPod()) {
            return;
        }

        avtaleRepository.findAvtalerForEndringAvStatus().forEach(avtale -> {
            Status status = Status.fra(avtale);
            if (avtale.getStatus().equals(status)) {
                return;
            }

            log.info(
                "Avtale med id {} oppdateres med ny status {}. Tidligere status var {}.",
                avtale.getId(),
                status,
                avtale.getStatus()
            );
            avtale.setStatus(status);
            avtaleRepository.save(avtale);
        });
    }

}
