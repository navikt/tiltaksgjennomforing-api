package no.nav.tag.tiltaksgjennomforing.avtale.jobber;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.service.GjeldendeTilskuddsperiodeJobbService;
import no.nav.tag.tiltaksgjennomforing.leader.LeaderPodCheck;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({Miljø.DEV_FSS})
class GjeldendeTilskuddsperiodeJobb {
    private final GjeldendeTilskuddsperiodeJobbService gjeldendeTilskuddsperiodeJobbService;
    private final LeaderPodCheck leaderPodCheck;

    public GjeldendeTilskuddsperiodeJobb(GjeldendeTilskuddsperiodeJobbService gjeldendeTilskuddsperiodeJobbService, LeaderPodCheck leaderPodCheck) {
        this.gjeldendeTilskuddsperiodeJobbService = gjeldendeTilskuddsperiodeJobbService;
        this.leaderPodCheck = leaderPodCheck;
    }

    @Scheduled(cron = "0 30 0 * * *")
    public void settGjeldendeTilskuddsperiodeJobb() {
        if (leaderPodCheck.isLeaderPod()) {
            log.info("Jobb for å oppdatere gjeldedeTilskuddsperiode-felt startet...");

            Page<Avtale> page = Page.empty();
            do {
                page = gjeldendeTilskuddsperiodeJobbService.settGjeldendeTilskuddsperiodeJobb(
                    page.isEmpty() ? PageRequest.of(0, 500, Sort.by(Sort.Direction.ASC, "id")) : page.nextPageable()
                );
            } while(page.hasNext());

            log.info(
                "Jobb for å oppdatere gjeldedeTilskuddsperiode-felt fullført! Behandlet {} avtaler.",
                page.getTotalElements()
            );
        }
    }
}
