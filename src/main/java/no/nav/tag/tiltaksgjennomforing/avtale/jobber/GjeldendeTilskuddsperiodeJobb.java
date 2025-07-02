package no.nav.tag.tiltaksgjennomforing.avtale.jobber;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.service.gjeldendetilskuddsperiode.GjeldendeTilskuddsperiodeJobbService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({ Miljø.DEV_FSS, Miljø.PROD_FSS })
class GjeldendeTilskuddsperiodeJobb {
    private final GjeldendeTilskuddsperiodeJobbService gjeldendeTilskuddsperiodeJobbService;

    public GjeldendeTilskuddsperiodeJobb(GjeldendeTilskuddsperiodeJobbService gjeldendeTilskuddsperiodeJobbService) {
        this.gjeldendeTilskuddsperiodeJobbService = gjeldendeTilskuddsperiodeJobbService;
    }

    @Scheduled(cron = "0 30 0 * * *")
    @SchedulerLock(name = "GjeldendeTilskuddsperiodeJobb_settGjeldendeTilskuddsperiodeJobb", lockAtLeastFor = "PT30M", lockAtMostFor = "PT60M")
    public void settGjeldendeTilskuddsperiodeJobb() {
            gjeldendeTilskuddsperiodeJobbService.start();
    }
}
