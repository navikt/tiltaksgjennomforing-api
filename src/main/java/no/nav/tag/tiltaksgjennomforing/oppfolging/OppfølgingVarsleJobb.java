package no.nav.tag.tiltaksgjennomforing.oppfolging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleHendelseUtførtAvRolle;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.tag.tiltaksgjennomforing.varsel.Varsel;
import no.nav.tag.tiltaksgjennomforing.varsel.VarselRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OppfølgingVarsleJobb {

    private final AvtaleRepository avtaleRepository;
    private final VarselRepository varselRepository;


    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    @SchedulerLock(name = "AvtalestatusEndretJobb_run", lockAtLeastFor = "PT20S", lockAtMostFor = "PT30S")
    public void varsleVeilederOmOppfølging() {
        LocalDate dagenDato = Now.localDate();
        List<Avtale> avtaler = avtaleRepository.finnAvtalerSomSnartSkalFølgesOpp(dagenDato);
        if (!avtaler.isEmpty()) log.info("Fant {} avtaler som krever oppfølging. Oppretter varsel til veileder på disse.", avtaler.size());
        avtaler.forEach(avtale -> {
            Varsel varsel = Varsel.nyttVarsel(avtale.getVeilederNavIdent(), true, avtale, Avtalerolle.VEILEDER, AvtaleHendelseUtførtAvRolle.SYSTEM, Identifikator.SYSTEM, HendelseType.OPPFØLGING_AV_TILTAK_KREVES, null);
            varselRepository.save(varsel);
            avtale.setOppfolgingVarselSendt(Now.instant());
        });
        avtaleRepository.saveAll(avtaler);
    }
}
