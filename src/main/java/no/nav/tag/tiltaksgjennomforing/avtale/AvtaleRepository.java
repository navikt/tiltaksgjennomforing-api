package no.nav.tag.tiltaksgjennomforing.avtale;

import io.micrometer.core.annotation.Timed;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

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
    List<Avtale> findAllByBedriftNrIn(Set<BedriftNr> bedriftNrList);

    @Timed(percentiles = { 0.5d, 0.75d, 0.9d, 0.99d, 0.999d })
    List<Avtale> findAllByDeltakerFnr(Fnr deltakerFnr);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    List<Avtale> findAllByVeilederNavIdent(NavIdent veilederNavIdent);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Override
    List<Avtale> findAll();

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Override
    Avtale save(Avtale entity);

    @Query(value = "SELECT DISTINCT  * FROM AVTALE "
        + "WHERE AVTALE.tiltakstype not in ('ARBEIDSTRENING') "
        + "AND EXISTS (SELECT * FROM AVTALE_INNHOLD "
        + "WHERE AVTALE.ID = AVTALE_INNHOLD.AVTALE "
        + "AND AVTALE_INNHOLD.GODKJENT_AV_VEILEDER is not null "
        + "AND EXISTS (SELECT * FROM TILSKUDD_PERIODE "
        + "WHERE TILSKUDD_PERIODE.STATUS = ?1 "
        + "AND TILSKUDD_PERIODE.AVTALE_INNHOLD = AVTALE_INNHOLD.ID))"
        + "AND ENHET_OPPFOLGING IN (?2) "
        + "OR ENHET_GEOGRAFISK IN (?2);", nativeQuery = true)
    List<Avtale> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheter(String tilskuddsperiodestatus, Set<String> navEnheter);

}

