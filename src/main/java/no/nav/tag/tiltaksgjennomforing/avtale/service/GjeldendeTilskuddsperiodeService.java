package no.nav.tag.tiltaksgjennomforing.avtale.service;

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
    public Slice<Avtale> settGjeldendeTilskuddsperiode(Pageable pageable) {
        Slice<Avtale> slice = hentAvtaler(pageable);
        List<Avtale> avtaler = slice.getContent();
        if (avtaler.isEmpty()) {
            log.info("Ingen avtaler å behandle");
            return slice;
        }
        log.info("Behandler {} avtaler...", avtaler.size());
        avtaler.forEach(avtale -> {
            var nyGjeldende = TilskuddPeriode.finnGjeldende(avtale);
            var gjeldendeTilskuddsperiode = avtale.getGjeldendeTilskuddsperiode(false);

            var erLikGjeldende = Objects.equals(nyGjeldende, gjeldendeTilskuddsperiode);
            if (erLikGjeldende) {
                log.info(
                    "Avtale med id: {} har allerede riktig gjeldende tilskuddsperiode: {}",
                    avtale.getId(),
                    Optional.ofNullable(nyGjeldende).map(TilskuddPeriode::getId).orElse(null)
                );
            } else {
                log.info("Oppdaterer gjeldende tilskuddsperiode for avtale med id: {}", avtale.getId());
                avtale.setGjeldendeTilskuddsperiode(nyGjeldende);
                avtaleRepository.save(avtale);
            }
        });
        return slice;
    }
}
