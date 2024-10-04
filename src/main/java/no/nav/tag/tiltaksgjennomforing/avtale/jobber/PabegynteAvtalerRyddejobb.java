package no.nav.tag.tiltaksgjennomforing.avtale.jobber;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleUtlopHandling;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile({ Miljø.LOCAL, Miljø.DEV_FSS, Miljø.PROD_FSS })
public class PabegynteAvtalerRyddejobb {
    private final FeatureToggleService featureToggleService;
    private final AvtaleRepository avtaleRepository;

    public PabegynteAvtalerRyddejobb(
        FeatureToggleService featureToggleService,
        AvtaleRepository avtaleRepository
    ) {
        this.featureToggleService = featureToggleService;
        this.avtaleRepository = avtaleRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void run() {
        List<Avtale> avtaler = avtaleRepository.findAvtalerSomErPabegyntEllerManglerGodkjenning();

        if (avtaler.isEmpty()) {
            return;
        }

        List<AvtaleUtlopHandling> handlinger = avtaler.stream()
            .map(AvtaleUtlopHandling::parse)
            .toList();

        if (!featureToggleService.isEnabled(FeatureToggle.PABEGYNT_AVTALE_RYDDE_JOBB)) {
            log.info(
                "Ryddejobben er skrudd av! Om den hadde vært på ville: " +
                "{} avtale(r) utløpt, {} avtale(r) fått 24 timers varsel, {} avtale(r) fått 1 ukes varsel.",
                handlinger.stream().filter(handling -> handling == AvtaleUtlopHandling.UTLOP).count(),
                handlinger.stream().filter(handling -> handling == AvtaleUtlopHandling.VARSEL_24_TIMER).count(),
                handlinger.stream().filter(handling -> handling == AvtaleUtlopHandling.VARSEL_EN_UKE).count()
            );
            return;
        }

        log.info(
            "Rydder avtaler som er påbegynt eller mangler godkjenning: " +
            "{} avtale(r) utløper, {} avtale(r) får 24 timers varsel, {} avtale(r) får 1 ukes varsel.",
            handlinger.stream().filter(handling -> handling == AvtaleUtlopHandling.UTLOP).count(),
            handlinger.stream().filter(handling -> handling == AvtaleUtlopHandling.VARSEL_24_TIMER).count(),
            handlinger.stream().filter(handling -> handling == AvtaleUtlopHandling.VARSEL_EN_UKE).count()
        );

        avtaler.forEach(avtale -> {
            switch (AvtaleUtlopHandling.parse(avtale)) {
                case VARSEL_EN_UKE -> {
                    avtale.utlop(AvtaleUtlopHandling.VARSEL_EN_UKE);
                    avtaleRepository.save(avtale);
                }
                case VARSEL_24_TIMER -> {
                    avtale.utlop(AvtaleUtlopHandling.VARSEL_24_TIMER);
                    avtaleRepository.save(avtale);
                }
                case UTLOP -> {
                    log.info(
                        "Utløper avtale {} med status {} som sist var endret {}",
                        avtale.getId(),
                        avtale.statusSomEnum(),
                        avtale.getSistEndret()
                    );
                    avtale.utlop(AvtaleUtlopHandling.UTLOP);
                    avtaleRepository.save(avtale);
                }
            }
        });
    }
}
