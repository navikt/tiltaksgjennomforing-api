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
import java.util.UUID;

@Slf4j
@Service
public class GjeldendeTilskuddsperiodeService {
    private final AvtaleRepository avtaleRepository;

    public GjeldendeTilskuddsperiodeService(AvtaleRepository avtaleRepository) {
        this.avtaleRepository = avtaleRepository;
    }

    @Transactional
    public SettGjeldendeTilskuddsperiodeRespons settGjeldendeTilskuddsperiode(UUID fraId, Pageable pageable) {
        Slice<UUID> idSlice = avtaleRepository.finnAvtaleIderMedAktiveTilskuddsperioder(fraId, pageable);
        List<UUID> ider = idSlice.getContent();

        if (ider.isEmpty()) {
            log.debug("Ingen avtaler å behandle");
            return new SettGjeldendeTilskuddsperiodeRespons(fraId, false, 0, 0);
        }

        UUID sisteId = ider.getLast();
        List<Avtale> avtaler = avtaleRepository.findAllById(ider);
        log.debug("Behandler {} avtaler...", avtaler.size());

        int antallOppdatert = 0;
        int antallIkkeOppdatert = 0;
        for (Avtale avtale : avtaler) {
            var utledetGjeldendePeriode = TilskuddPeriode.utledGjeldendeTilskuddsperiode(avtale);
            var nyGjeldende = utledetGjeldendePeriode.tilskuddPeriode();
            var gjeldendeTilskuddsperiode = avtale.getGjeldendeTilskuddsperiode();

            if (Objects.equals(nyGjeldende, gjeldendeTilskuddsperiode)) {
                log.debug(
                    "Avtale med id: {} har allerede riktig gjeldende tilskuddsperiode: {}",
                    avtale.getId(),
                    Optional.ofNullable(nyGjeldende).map(TilskuddPeriode::getId).orElse(null)
                );
                antallIkkeOppdatert++;
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
                antallOppdatert++;
            }
        }
        return new SettGjeldendeTilskuddsperiodeRespons(sisteId, idSlice.hasNext(), antallOppdatert, antallIkkeOppdatert);
    }
}
