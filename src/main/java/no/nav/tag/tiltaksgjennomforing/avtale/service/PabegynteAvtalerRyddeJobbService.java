package no.nav.tag.tiltaksgjennomforing.avtale.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class PabegynteAvtalerRyddeJobbService {
    private static final Limit LIMIT = Limit.of(500);
    private final PabegynteAvtalerRyddeService pabegynteAvtalerRyddeService;
    private final FeatureToggleService featureToggleService;

    public PabegynteAvtalerRyddeJobbService(
        PabegynteAvtalerRyddeService pabegynteAvtalerRyddeService,
        FeatureToggleService featureToggleService
    ) {
        this.pabegynteAvtalerRyddeService = pabegynteAvtalerRyddeService;
        this.featureToggleService = featureToggleService;
    }

    public void ryddAvtalerSomErPabegyntEllerManglerGodkjenning() {
        boolean ryddejobbAktivert = featureToggleService.isEnabled(FeatureToggle.PABEGYNT_AVTALE_RYDDE_JOBB);

        int antallUtlop = 0;
        int antallVarsel24Timer = 0;
        int antallVarselEnUke = 0;
        UUID sisteId = new UUID(0L, 0L);
        boolean harFlere = true;

        while (harFlere) {
            PabegynteAvtalerRyddeBatchRespons respons = pabegynteAvtalerRyddeService.ryddBatch(sisteId, LIMIT, ryddejobbAktivert);
            sisteId = respons.sisteId();
            harFlere = respons.harFlere();
            antallUtlop += respons.antallUtlop();
            antallVarsel24Timer += respons.antallVarsel24Timer();
            antallVarselEnUke += respons.antallVarselEnUke();
        }

        log.info(
            "{}: {} avtale(r) utløper, {} avtale(r) får 24 timers varsel, {} avtale(r) får 1 ukes varsel.",
            ryddejobbAktivert ? "Rydder avtaler som er påbegynt eller mangler godkjenning" : "Ryddejobben er skrudd av! Om den hadde vært på ville",
            antallUtlop, antallVarsel24Timer, antallVarselEnUke
        );
    }
}
