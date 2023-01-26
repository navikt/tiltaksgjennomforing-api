package no.nav.tag.tiltaksgjennomforing.arenamigrering;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Service
public class ArenaMigreringService {

    private final AvtaleRepository avtaleRepository;

    @Async
    public void lagTilskuddsperioderPåArenaAvtaler(LocalDate migreringsDato) {
        AtomicInteger antallMigrert = new AtomicInteger();
        List<Avtale> midlertidigLønnstilskuddAvtaler = avtaleRepository.findAllByTiltakstype(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        log.info("Oppdaterer tilskuddsperioder på (sånn ca, før filtrering på piloter) {} avtaler for midlertidig lønnstilskudd", midlertidigLønnstilskuddAvtaler.size());
        midlertidigLønnstilskuddAvtaler.forEach(avtale -> {
            migrerEnAvtaleTransactional(migreringsDato, antallMigrert, avtale);
        });

        List<Avtale> varigLønnstilskuddAvtaler = avtaleRepository.findAllByTiltakstype(Tiltakstype.VARIG_LONNSTILSKUDD);
        log.info("Oppdaterer tilskuddsperioder på (sånn ca, før filtrering på piloter) {} avtaler for varig lønnstilskudd", varigLønnstilskuddAvtaler.size());
        varigLønnstilskuddAvtaler.forEach(avtale -> {
            migrerEnAvtaleTransactional(migreringsDato, antallMigrert, avtale);
        });
        log.info("Migrering av tilskuddsperioder for gamle avtaler i arena fullført. Totalt antall: {}", antallMigrert.get());
    }

    @Transactional
    void migrerEnAvtaleTransactional(LocalDate migreringsDato, AtomicInteger antallMigrert, Avtale avtale) {
        if(avtale.nyeTilskuddsperioderVedMigreringFraArena(migreringsDato, false)) {
            avtaleRepository.save(avtale);
            antallMigrert.getAndIncrement();
        }
        if(antallMigrert.get() % 100 == 0) {
            log.info("Migrert {} antall avtaler", antallMigrert.get());
        }
    }

    @Async
    public void lagTilskuddsperioderPåArenaAvtalerDryRun(LocalDate migreringsDato) {
        AtomicInteger antallMigrert = new AtomicInteger();
        List<Avtale> midlertidigLønnstilskuddAvtaler = avtaleRepository.findAllByTiltakstype(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);

        log.info("DRY - Oppdaterer tilskuddsperioder på (sånn ca, før filtrering på piloter) {} avtaler for midlertidig lønnstilskudd", midlertidigLønnstilskuddAvtaler.size());

        midlertidigLønnstilskuddAvtaler.forEach(avtale -> {
            if(avtale.nyeTilskuddsperioderVedMigreringFraArena(migreringsDato, true)) {
                antallMigrert.getAndIncrement();
            }
            if(antallMigrert.get() % 100 == 0) {
                log.info("DRY - Migrert {} antall avtaler", antallMigrert.get());
            }
        });

        List<Avtale> varigLønnstilskuddAvtaler = avtaleRepository.findAllByTiltakstype(Tiltakstype.VARIG_LONNSTILSKUDD);

        log.info("DRY - Oppdaterer tilskuddsperioder på (sånn ca, før filtrering på piloter) {} avtaler for varig lønnstilskudd", varigLønnstilskuddAvtaler.size());

        varigLønnstilskuddAvtaler.forEach(avtale -> {
            if(avtale.nyeTilskuddsperioderVedMigreringFraArena(migreringsDato, true)) {
                antallMigrert.getAndIncrement();
            }
            if(antallMigrert.get() % 100 == 0) {
                log.info("DRY - Migrert {} antall avtaler", antallMigrert.get());
            }
        });
        log.info("DRY - Migrering av tilskuddsperioder for gamle avtaler i arena fullført. {}", antallMigrert.get());
    }

}
