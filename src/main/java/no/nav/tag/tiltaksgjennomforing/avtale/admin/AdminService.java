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
import no.nav.tag.tiltaksgjennomforing.varsel.Varsel;
import no.nav.tag.tiltaksgjennomforing.varsel.VarselFactory;
import no.nav.tag.tiltaksgjennomforing.varsel.VarselRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final AvtaleRepository avtaleRepository;
    private final VarselRepository varselRepository;

    @Async
    @Transactional
    public void oppdaterteAvtalekrav() {
        AtomicInteger antallSendt = new AtomicInteger();
        AtomicInteger antallIgnorert = new AtomicInteger();

        log.info("Oppdaterer avtalekrav...");

        var avtaler = avtaleRepository.findAllByGjeldendeInnhold_GodkjentAvArbeidsgiverNotNullAndStatusIn(
            List.of(Status.GJENNOMFØRES, Status.MANGLER_GODKJENNING, Status.AVSLUTTET)
        );
        log.info("Fant {} avtaler som det skal sendes varslinger på", avtaler.size());

        List<Varsel> nyeVarsler = new ArrayList<>();

        avtaler.forEach(avtale -> {
            List<Varsel> arbeidsgiverVarsler = varselRepository.findAllByAvtaleIdAndMottaker(
                avtale.getId(),
                Avtalerolle.ARBEIDSGIVER
            );
            boolean harAlleredeAvtalekravVarsel = arbeidsgiverVarsler.stream()
                .anyMatch(varsel -> varsel.getHendelseType().equals(HendelseType.OPPDATERTE_AVTALEKRAV));

            if (harAlleredeAvtalekravVarsel) {
                var ignorerte = antallIgnorert.getAndIncrement();
                if (ignorerte % 100 == 0) {
                    log.info(
                        "Fant avtale som allerede har avtalekrav-varsel, ignorerer. Foreløpig {} ignorerte",
                        ignorerte
                    );
                }
            } else {
                var nyttVarselForAvtale = lagHendelse(avtale);
                antallSendt.getAndIncrement();
                if (antallSendt.get() % 100 == 0) {
                    log.info("Opprettet varsel for avtalekrav for {} avtaler", antallSendt.get());
                }
                nyeVarsler.add(nyttVarselForAvtale);
            }
        });

        varselRepository.saveAll(nyeVarsler);
        log.info("Lagret {} varsler, hoppet over {} avtaler", nyeVarsler.size(), antallIgnorert.get());
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
