package no.nav.tag.tiltaksgjennomforing.oppfolging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
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
    public void varsleVeilederOmOppfølging() {
        LocalDate dagenDato = Now.localDate();
        List<Avtale> avtaler = avtaleRepository.findAllByKreverOppfolgingFomLessThanAndOppfolgingVarselSendtIsNull(dagenDato);
        log.info("Fant {} avtaler som krever oppfølging. Oppretter varsel på disse.", avtaler.size());
        avtaler.forEach(avtale -> {
            //TODO: Send varsel til veileder
            Varsel varsel = Varsel.nyttVarsel(avtale.getVeilederNavIdent(), true, avtale, Avtalerolle.VEILEDER, AvtaleHendelseUtførtAvRolle.SYSTEM, Identifikator.SYSTEM, HendelseType.OPPFØLGING_KREVES_VARSEL, null);
            varselRepository.save(varsel);
            avtale.setOppfolgingVarselSendt(Now.instant());
        });
        avtaleRepository.saveAll(avtaler);
    }
}
