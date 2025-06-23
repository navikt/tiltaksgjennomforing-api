package no.nav.tag.tiltaksgjennomforing.avtale.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class GjeldendeTilskuddsperiodeService {
    private final AvtaleRepository avtaleRepository;

    public GjeldendeTilskuddsperiodeService(AvtaleRepository avtaleRepository) {
        this.avtaleRepository = avtaleRepository;
    }

    public Slice<Avtale> hentAvtaler(Pageable page) {
        return avtaleRepository.finnAvtaleMedAktiveTilskuddsperioder(Now.localDate().plusMonths(3), page);
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
            var gjeldendeTilskuddsperiode = avtale.getGjeldendeTilskuddsperiode(false);
            var nyGjeldende = TilskuddPeriode.finnGjeldende(avtale);
            if (gjeldendeTilskuddsperiode == null) {
                log.info(
                    "Avtale med id: {} har ingen gjeldende tilskuddsperiode, setter ny gjeldende tilskuddsperiode",
                    avtale.getId()
                );
            }
            var erLikGjeldende = Optional.ofNullable(nyGjeldende)
                .map(tp -> tp.equals(gjeldendeTilskuddsperiode))
                .orElse(nyGjeldende == null && gjeldendeTilskuddsperiode == null);
            if (!erLikGjeldende) {
                log.info("Oppdaterer gjeldende tilskuddsperiode for avtale med id: {}", avtale.getId());
                avtale.setGjeldendeTilskuddsperiode(nyGjeldende);
            }
        });
        avtaleRepository.saveAll(avtaler);
        return slice;
    }
}
