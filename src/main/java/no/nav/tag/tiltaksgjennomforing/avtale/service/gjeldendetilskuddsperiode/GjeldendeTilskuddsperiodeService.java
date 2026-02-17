package no.nav.tag.tiltaksgjennomforing.avtale.service.gjeldendetilskuddsperiode;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class GjeldendeTilskuddsperiodeService {
    private final AvtaleRepository avtaleRepository;

    public GjeldendeTilskuddsperiodeService(AvtaleRepository avtaleRepository) {
        this.avtaleRepository = avtaleRepository;
    }

    public Slice<Avtale> hentAvtaler(Pageable page) {
        return avtaleRepository.finnAvtaleMedAktiveTilskuddsperioder(page);
    }

    @Transactional
    public SettGjeldendeTilskuddsperiodeRespons settGjeldendeTilskuddsperiode(Pageable pageable) {
        AtomicInteger antallOppdatert = new AtomicInteger();
        AtomicInteger antallIkkeOppdatert = new AtomicInteger();
        Slice<Avtale> slice = hentAvtaler(pageable);
        List<Avtale> avtaler = slice.getContent();

        if (avtaler.isEmpty()) {
            log.debug("Ingen avtaler å behandle");
            return new SettGjeldendeTilskuddsperiodeRespons(slice, antallOppdatert.get(), antallIkkeOppdatert.get());
        }
        log.debug("Behandler {} avtaler...", avtaler.size());
        avtaler.forEach(avtale -> {
            var utledetGjeldendePeriode = TilskuddPeriode.utledGjeldendeTilskuddsperiode(avtale);
            var nyGjeldende = utledetGjeldendePeriode.tilskuddPeriode();
            var gjeldendeTilskuddsperiode = avtale.getGjeldendeTilskuddsperiode();

            var erLikGjeldende = Objects.equals(nyGjeldende, gjeldendeTilskuddsperiode);
            if (erLikGjeldende) {
                log.debug(
                    "Avtale med id: {} har allerede riktig gjeldende tilskuddsperiode: {}",
                    avtale.getId(),
                    Optional.ofNullable(nyGjeldende).map(TilskuddPeriode::getId).orElse(null)
                );
                antallIkkeOppdatert.getAndIncrement();
            } else {
                log.info(
                    "Oppdaterer gjeldende tilskuddsperiode på avtale {} med status {} " +
                    "fra tilskuddsperiode [{},{},{}] til tilskuddsperiode [{},{},{}]. " +
                    "Med forklaring: {}",
                    avtale.getId(),
                    avtale.getStatus(),
                    Optional.ofNullable(gjeldendeTilskuddsperiode).map(TilskuddPeriode::getId).orElse(null),
                    Optional.ofNullable(gjeldendeTilskuddsperiode).map(TilskuddPeriode::getLøpenummer).orElse(null),
                    Optional.ofNullable(gjeldendeTilskuddsperiode).map(TilskuddPeriode::getStatus).orElse(null),
                    Optional.ofNullable(nyGjeldende).map(TilskuddPeriode::getId).orElse(null),
                    Optional.ofNullable(nyGjeldende).map(TilskuddPeriode::getLøpenummer).orElse(null),
                    Optional.ofNullable(nyGjeldende).map(TilskuddPeriode::getStatus).orElse(null),
                    utledetGjeldendePeriode.forklaring()
                );
                avtale.setGjeldendeTilskuddsperiode(nyGjeldende);
                avtaleRepository.save(avtale);
                antallOppdatert.getAndIncrement();
            }
        });
        return new SettGjeldendeTilskuddsperiodeRespons(slice, antallOppdatert.get(), antallIkkeOppdatert.get());
    }
}
