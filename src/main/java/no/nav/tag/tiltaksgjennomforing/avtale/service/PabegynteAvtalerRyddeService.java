package no.nav.tag.tiltaksgjennomforing.avtale.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleUtlopHandling;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Profile({ Miljø.DEV_FSS, Miljø.PROD_FSS })
public class PabegynteAvtalerRyddeService {
    private final AvtaleRepository avtaleRepository;
    private final FeatureToggleService featureToggleService;

    public PabegynteAvtalerRyddeService(
        AvtaleRepository avtaleRepository,
        FeatureToggleService featureToggleService
    ) {
        this.featureToggleService = featureToggleService;
        this.avtaleRepository = avtaleRepository;
    }

    @Transactional
    public void ryddAvtalerSomErPabegyntEllerManglerGodkjenning() {
        List<Avtale> avtaler = avtaleRepository.findAvtalerSomErPabegyntEllerManglerGodkjenning();

        if (avtaler.isEmpty()) {
            return;
        }

        Map<AvtaleUtlopHandling, List<Avtale>> avtaleHandling = avtaler.stream()
            .collect(Collectors.groupingBy(AvtaleUtlopHandling::parse));

        if (!featureToggleService.isEnabled(FeatureToggle.PABEGYNT_AVTALE_RYDDE_JOBB)) {
            log.info(
                "Ryddejobben er skrudd av! Om den hadde vært på ville: " +
                "{} avtale(r) utløpt, {} avtale(r) fått 24 timers varsel, {} avtale(r) fått 1 ukes varsel.",
                Optional.ofNullable(avtaleHandling.get(AvtaleUtlopHandling.UTLOP)).map(List::size).orElse(0),
                Optional.ofNullable(avtaleHandling.get(AvtaleUtlopHandling.VARSEL_24_TIMER)).map(List::size).orElse(0),
                Optional.ofNullable(avtaleHandling.get(AvtaleUtlopHandling.VARSEL_EN_UKE)).map(List::size).orElse(0)
            );
            return;
        }

        log.info(
            "Rydder avtaler som er påbegynt eller mangler godkjenning: " +
            "{} avtale(r) utløper, {} avtale(r) får 24 timers varsel, {} avtale(r) får 1 ukes varsel.",
            Optional.ofNullable(avtaleHandling.get(AvtaleUtlopHandling.UTLOP)).map(List::size).orElse(0),
            Optional.ofNullable(avtaleHandling.get(AvtaleUtlopHandling.VARSEL_24_TIMER)).map(List::size).orElse(0),
            Optional.ofNullable(avtaleHandling.get(AvtaleUtlopHandling.VARSEL_EN_UKE)).map(List::size).orElse(0)
        );

        avtaleHandling.forEach((handling, avtaleliste) -> {
            switch (handling) {
                case VARSEL_EN_UKE -> avtaleliste.forEach(avtale -> {
                    avtale.utlop(AvtaleUtlopHandling.VARSEL_EN_UKE);
                    avtaleRepository.save(avtale);
                });
                case VARSEL_24_TIMER -> avtaleliste.forEach(avtale -> {
                    avtale.utlop(AvtaleUtlopHandling.VARSEL_24_TIMER);
                    avtaleRepository.save(avtale);
                });
                case UTLOP -> avtaleliste.forEach(avtale -> {
                    log.info(
                        "Utløper avtale {} med status {} som sist var endret {}",
                        avtale.getId(),
                        avtale.getStatus(),
                        avtale.getSistEndret()
                    );
                    avtale.utlop(AvtaleUtlopHandling.UTLOP);
                    avtaleRepository.save(avtale);
                });
            }
        });

        System.gc();
    }
}
