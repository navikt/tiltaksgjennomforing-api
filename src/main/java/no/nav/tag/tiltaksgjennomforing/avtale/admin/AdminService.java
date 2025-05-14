package no.nav.tag.tiltaksgjennomforing.avtale.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleHendelseUtførtAvRolle;
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
                    && avtaleGodkjentAvArbeidsgiver.isBefore(avtalekravDato)
                    // trenger ikke varsle om endringer i krav på avtaler som er eldre enn 12 uker
                    && !eldreEnn12UkerOgAvsluttet;

                if (!skalBehandles) {
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

                var antallBehandletTeller = antallBehandlet.getAndIncrement();
                if (antallBehandletTeller % 100 == 0) {
                    log.info("Behandlet {} avtaler", antallBehandletTeller);
                }
            });

            log.info(
                "Lagret {} varsler, hoppet over {} avtaler",
                antallSendt.get(),
                antallIgnorertPgaEksisterendeVarsel.get()
            );
        }
    }

    private Varsel lagHendelse(Avtale avtale) {
        VarselFactory factory = new VarselFactory(
            avtale,
            AvtaleHendelseUtførtAvRolle.SYSTEM,
            Identifikator.SYSTEM,
            HendelseType.OPPDATERTE_AVTALEKRAV
        );
        return factory.arbeidsgiver();
    }
}
