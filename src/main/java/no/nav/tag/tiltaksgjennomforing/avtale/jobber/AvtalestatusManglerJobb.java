package no.nav.tag.tiltaksgjennomforing.avtale.jobber;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.leader.LeaderPodCheck;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Profile({ Miljø.DEV_FSS, Miljø.PROD_FSS })
public class AvtalestatusManglerJobb {

    private final AvtaleRepository avtaleRepository;
    private final LeaderPodCheck leaderPodCheck;

    public AvtalestatusManglerJobb(
        AvtaleRepository avtaleRepository,
        LeaderPodCheck leaderPodCheck
    ) {
        this.avtaleRepository = avtaleRepository;
        this.leaderPodCheck = leaderPodCheck;
    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void run() {
        if (!leaderPodCheck.isLeaderPod()) {
            return;
        }

        List<Avtale> avtaler = avtaleRepository.findAvtalerSomIkkeHarStatus();

        if (avtaler.isEmpty()) {
            log.info("Avtalejobben er ferdig og alle avtaler har status - Jobben kan slettes");
            return;
        }

        log.info("Avtalejobben oppdaterer {} avtaler som mangler status", avtaler.size());

        avtaler.forEach(avtale -> {
            avtale.setStatus(Status.fra(avtale));
            avtaleRepository.save(avtale);
        });
    }
}
