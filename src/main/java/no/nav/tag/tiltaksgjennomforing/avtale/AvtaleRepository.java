package no.nav.tag.tiltaksgjennomforing.avtale;

import io.micrometer.core.annotation.Timed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvtaleRepository extends JpaRepository<Avtale, UUID>, JpaSpecificationExecutor {
    @Timed(percentiles = { 0.5d, 0.75d, 0.9d, 0.99d, 0.999d })
    @Override
    Optional<Avtale> findById(UUID id);

    @Timed(percentiles = { 0.5d, 0.75d, 0.9d, 0.99d, 0.999d })
    @Override
    List<Avtale> findAllById(Iterable<UUID> ids);

    @Timed(percentiles = { 0.5d, 0.75d, 0.9d, 0.99d, 0.999d })
    List<Avtale> findAllByBedriftNr(BedriftNr bedriftNr);

    @Timed(percentiles = { 0.5d, 0.75d, 0.9d, 0.99d, 0.999d })
    List<Avtale> findAllByBedriftNrIn(List<BedriftNr> bedriftNrList);

    @Timed(percentiles = { 0.5d, 0.75d, 0.9d, 0.99d, 0.999d })
    List<Avtale> findAllByDeltakerFnr(Fnr deltakerFnr);

    @Timed(percentiles = { 0.5d, 0.75d, 0.9d, 0.99d, 0.999d })
    List<Avtale> findAllByVeilederNavIdent(NavIdent veilederNavIdent);

    @Timed(percentiles = { 0.5d, 0.75d, 0.9d, 0.99d, 0.999d })
    @Override
    List<Avtale> findAll();

    @Timed(percentiles = { 0.5d, 0.75d, 0.9d, 0.99d, 0.999d })
    @Override
    Avtale save(Avtale entity);
}

