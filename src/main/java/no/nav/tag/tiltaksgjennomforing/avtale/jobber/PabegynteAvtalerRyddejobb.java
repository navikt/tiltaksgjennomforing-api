package no.nav.tag.tiltaksgjennomforing.avtale.jobber;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleUtlopHandling;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.leader.LeaderPodCheck;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.tag.tiltaksgjennomforing.avtale.AvtaleUtlopHandling.START_DATO_FOR_RYDDING;

@Slf4j
@Component
@Profile({ Miljø.DEV_FSS, Miljø.PROD_FSS })
public class PabegynteAvtalerRyddejobb {
    private List<Status> avtaleStatuserSomSkalRyddes = List.of(Status.PÅBEGYNT, Status.MANGLER_GODKJENNING);

    private final AvtaleRepository avtaleRepository;
    private final FeatureToggleService featureToggleService;
    private final LeaderPodCheck leaderPodCheck;

    public PabegynteAvtalerRyddejobb(
        AvtaleRepository avtaleRepository,
        FeatureToggleService featureToggleService,
        LeaderPodCheck leaderPodCheck
    ) {
        this.featureToggleService = featureToggleService;
        this.avtaleRepository = avtaleRepository;
        this.leaderPodCheck = leaderPodCheck;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void run() {
        if (!leaderPodCheck.isLeaderPod()) {
            return;
        }

        List<Avtale> avtaler = avtaleRepository.findAvtalerSomErPabegyntEllerManglerGodkjenning().stream()
            .filter(avtale -> avtaleStatuserSomSkalRyddes.contains(avtale.statusSomEnum()))
            .toList();

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

        if (START_DATO_FOR_RYDDING.isAfter(Now.instant())) {
            log.info(
                "Det står {} avtaler i kø til å utløpe {}.",
                avtaler.stream().map(AvtaleUtlopHandling::parseDangerously).filter(AvtaleUtlopHandling.UTLOP::equals).count(),
                START_DATO_FOR_RYDDING.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE)
            );
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
                        avtale.statusSomEnum(),
                        avtale.getSistEndret()
                    );
                    avtale.utlop(AvtaleUtlopHandling.UTLOP);
                    avtaleRepository.save(avtale);
                });
            }
        });
    }
}
