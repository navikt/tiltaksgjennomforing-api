package no.nav.tag.tiltaksgjennomforing.avtale.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleHendelseUtførtAv;
import no.nav.tag.tiltaksgjennomforing.utils.DatoUtils;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.tag.tiltaksgjennomforing.varsel.Varsel;
import no.nav.tag.tiltaksgjennomforing.varsel.VarselFactory;
import no.nav.tag.tiltaksgjennomforing.varsel.VarselRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final AvtaleRepository avtaleRepository;
    private final VarselRepository varselRepository;

    Set<Status> avtalekravStatuser = Set.of(Status.GJENNOMFØRES, Status.MANGLER_GODKJENNING, Status.AVSLUTTET);

    @Async
    @Transactional
    public void oppdaterteAvtalekrav(LocalDateTime avtalekravDato) {
        AtomicInteger antallBehandlet = new AtomicInteger(0);
        AtomicInteger antallSendt = new AtomicInteger();
        AtomicInteger antallSomIkkeSkalBehandles = new AtomicInteger();
        AtomicInteger antallIgnorertPgaEksisterendeVarsel = new AtomicInteger();

        log.info("Oppdaterer avtalekrav...");

        try (Stream<Avtale> avtaler = avtaleRepository.streamAllByStatusIn(avtalekravStatuser)) {
            avtaler.forEach(avtale -> {
                var avtaleGodkjentAvArbeidsgiver = avtale.getGjeldendeInnhold().getGodkjentAvArbeidsgiver();

                boolean eldreEnn12UkerOgAvsluttet = avtale.getGjeldendeInnhold()
                    .getSluttDato()
                    .isBefore(Now.localDate().minusWeeks(12))
                    && avtale.getStatus().equals(Status.AVSLUTTET);

                boolean skalBehandles = avtaleGodkjentAvArbeidsgiver != null
                    // arbeidsgiver godkjente før vi endret avtalekravene
                    && DatoUtils.instantTilLocalDateTime(avtaleGodkjentAvArbeidsgiver).isBefore(avtalekravDato)
                    // trenger ikke varsle om endringer i krav på avtaler som er eldre enn 12 uker
                    && !eldreEnn12UkerOgAvsluttet;

                if (!skalBehandles) {
                    antallSomIkkeSkalBehandles.getAndIncrement();
                    return;
                }

                List<Varsel> arbeidsgiverVarsler = varselRepository.findAllByAvtaleIdAndMottaker(
                    avtale.getId(),
                    Avtalerolle.ARBEIDSGIVER
                );

                boolean harAlleredeAvtalekravVarsel = arbeidsgiverVarsler.stream()
                    .anyMatch(varsel -> varsel.getHendelseType().equals(HendelseType.OPPDATERTE_AVTALEKRAV));

                if (harAlleredeAvtalekravVarsel) {
                    antallIgnorertPgaEksisterendeVarsel.getAndIncrement();
                } else {
                    var nyttVarselForAvtale = lagHendelse(avtale);
                    antallSendt.getAndIncrement();
                    if (antallSendt.get() % 100 == 0) {
                        log.info("Opprettet varsel for avtalekrav for {} avtaler", antallSendt.get());
                    }
                    varselRepository.save(nyttVarselForAvtale);
                }

                var antallBehandletTeller = antallBehandlet.incrementAndGet();
                if (antallBehandletTeller % 100 == 0) {
                    log.info(
                        "Så langt behandlet {} avtaler, antall som ikke skal behandles: {}. Ignorert pga eksisterende varsel {}",
                        antallBehandletTeller,
                        antallSomIkkeSkalBehandles.get(),
                        antallIgnorertPgaEksisterendeVarsel.get()
                    );
                }
            });

            var ignorertPgaEksisterendeVarsel = antallIgnorertPgaEksisterendeVarsel.get();
            log.info(
                "Behandlet {} avtaler. Lagret {} varsler, hoppet over {} avtaler ({} med eksisterende varsel)",
                antallBehandlet.get(),
                antallSendt.get(),
                ignorertPgaEksisterendeVarsel + antallSomIkkeSkalBehandles.get(),
                ignorertPgaEksisterendeVarsel
            );
        }
    }

    private Varsel lagHendelse(Avtale avtale) {
        VarselFactory factory = new VarselFactory(
            avtale,
            AvtaleHendelseUtførtAv.system(Identifikator.SYSTEM),
            HendelseType.OPPDATERTE_AVTALEKRAV
        );
        return factory.arbeidsgiver();
    }
}
