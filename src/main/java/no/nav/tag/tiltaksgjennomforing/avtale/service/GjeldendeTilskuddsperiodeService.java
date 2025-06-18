package no.nav.tag.tiltaksgjennomforing.avtale.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class GjeldendeTilskuddsperiodeService {
    private final AvtaleRepository avtaleRepository;
    private EntityManager entityManager;

    public GjeldendeTilskuddsperiodeService(
        AvtaleRepository avtaleRepository,
        EntityManager entityManager
    ) {
        this.avtaleRepository = avtaleRepository;
        this.entityManager = entityManager;
    }

    public Slice<Avtale> hentAvtaler(Pageable page) {
        return avtaleRepository.finnAvtaleMedAktiveTilskuddsperioder(
            Set.of(
                Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
                Tiltakstype.VARIG_LONNSTILSKUDD,
                Tiltakstype.SOMMERJOBB,
                Tiltakstype.VTAO
            ),
            Set.of(Status.GJENNOMFØRES, Status.KLAR_FOR_OPPSTART),
            page
        );
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
            var nyGjeldende = avtale.finnGjeldendeTilskuddsperiode();
            var erLikeGjeldende = Optional.ofNullable(nyGjeldende)
                    .map(tilskuddPeriode -> tilskuddPeriode.equals(avtale.getGjeldendeTilskuddsperiode()))
                    .orElse(nyGjeldende == null && avtale.getGjeldendeTilskuddsperiode() == null);

            if (!erLikeGjeldende) {
                avtale.setGjeldendeTilskuddsperiode(nyGjeldende);
                avtaleRepository.save(avtale);
            }

            entityManager.detach(avtale);
        });
        return slice;
    }
}
