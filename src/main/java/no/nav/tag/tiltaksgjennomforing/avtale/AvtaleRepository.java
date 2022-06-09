package no.nav.tag.tiltaksgjennomforing.avtale;

import io.micrometer.core.annotation.Timed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AvtaleRepository extends JpaRepository<Avtale, UUID>, JpaSpecificationExecutor {
    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Override
    Optional<Avtale> findById(UUID id);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Override
    List<Avtale> findAllById(Iterable<UUID> ids);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    List<Avtale> findAllByBedriftNr(BedriftNr bedriftNr);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    List<Avtale> findAllByBedriftNrIn(Set<BedriftNr> bedriftNrList);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    List<Avtale> findAllByDeltakerFnr(Fnr deltakerFnr);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    List<Avtale> findAllByVeilederNavIdent(NavIdent veilederNavIdent);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Query("FROM Avtale "
            + "where veilederNavIdent is null "
            + "and (enhetOppfolging in (?1) or enhetGeografisk in (?1))")
    List<Avtale> findAllUfordelteByEnhet(String navEnhet);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Query("FROM Avtale "
            + "where enhetOppfolging in (?1) or enhetGeografisk in (?1)")
    List<Avtale> findAllFordelteOrUfordeltByEnhet(String navEnhet);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    List<Avtale>findAllByAvtaleNr(Integer avtaleNr);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Override
    List<Avtale> findAll();

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Override
    Avtale save(Avtale entity);

    @Query(value =
            "SELECT distinct AVTALE.* FROM AVTALE " +
                    "LEFT JOIN AVTALE_INNHOLD " +
                    "ON AVTALE.ID = AVTALE_INNHOLD.AVTALE " +
                    "WHERE AVTALE_INNHOLD.GODKJENT_AV_VEILEDER is not null " +
                    "AND AVTALE.tiltakstype not in ('ARBEIDSTRENING') " +
                    "AND (:tiltakstype is null or AVTALE.TILTAKSTYPE LIKE :tiltakstype) " +
                    "AND EXISTS (SELECT avtale_id, status, løpenummer, start_dato FROM TILSKUDD_PERIODE where avtale_id = AVTALE.ID AND " +
                    "(:tilskuddsperiodestatus LIKE 'UBEHANDLET' AND :tilskuddsperiodestatus = status AND " +
                    "((start_dato - INTERVAL 3 MONTH <= current_date) OR (løpenummer = 1 AND status LIKE 'UBEHANDLET')))) " +
                    "AND (AVTALE.ENHET_OPPFOLGING IN (:navEnheter) OR AVTALE.ENHET_GEOGRAFISK IN (:navEnheter))", nativeQuery = true)
    List<Avtale> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterUbehandlet(
            @Param("tilskuddsperiodestatus") String tilskuddsperiodestatus,
            @Param("navEnheter") Set<String> navEnheter,
            @Param("tiltakstype") String tiltakstype);

    @Query(value =
            "SELECT distinct AVTALE.* FROM AVTALE " +
                    "LEFT JOIN AVTALE_INNHOLD " +
                    "ON AVTALE.ID = AVTALE_INNHOLD.AVTALE " +
                    "WHERE AVTALE_INNHOLD.GODKJENT_AV_VEILEDER is not null " +
                    "AND AVTALE.tiltakstype not in ('ARBEIDSTRENING') " +
                    "AND (:tiltakstype is null or AVTALE.TILTAKSTYPE LIKE :tiltakstype) " +
                    "AND EXISTS (SELECT avtale_id, status FROM TILSKUDD_PERIODE where avtale_id = AVTALE.ID AND " +
                     "((:tilskuddsperiodestatus LIKE 'GODKJENT' AND :tilskuddsperiodestatus = status))) " +
                    "AND NOT EXISTS (SELECT avtale_id, status, løpenummer, start_dato FROM TILSKUDD_PERIODE where " +
                    "avtale_id = AVTALE.ID AND status LIKE 'UBEHANDLET' " +
                    "AND ((start_dato - INTERVAL 3 MONTH <= current_date) OR (løpenummer = 1 AND status LIKE 'UBEHANDLET'))) " +
                    "AND (AVTALE.ENHET_OPPFOLGING IN (:navEnheter) OR AVTALE.ENHET_GEOGRAFISK IN (:navEnheter))", nativeQuery = true)
    List<Avtale> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterGodkjent(
            @Param("tilskuddsperiodestatus") String tilskuddsperiodestatus,
            @Param("navEnheter") Set<String> navEnheter,
            @Param("tiltakstype") String tiltakstype);

    @Query(value =
            "SELECT distinct AVTALE.* FROM AVTALE " +
                    "LEFT JOIN AVTALE_INNHOLD " +
                    "ON AVTALE.ID = AVTALE_INNHOLD.AVTALE " +
                    "WHERE AVTALE_INNHOLD.GODKJENT_AV_VEILEDER is not null " +
                    "AND AVTALE.tiltakstype not in ('ARBEIDSTRENING') " +
                    "AND (:tiltakstype is null or AVTALE.TILTAKSTYPE LIKE :tiltakstype) " +
                    "AND EXISTS (SELECT avtale_id, status, løpenummer, start_dato FROM TILSKUDD_PERIODE where avtale_id = AVTALE.ID AND " +
                    "(:tilskuddsperiodestatus LIKE 'AVSLÅTT' AND :tilskuddsperiodestatus = status) " +
                    "AND ((start_dato - INTERVAL 3 MONTH <= current_date) OR (løpenummer = 1 AND status LIKE 'UBEHANDLET'))) " +
                    "AND (AVTALE.ENHET_OPPFOLGING IN (:navEnheter) OR AVTALE.ENHET_GEOGRAFISK IN (:navEnheter))", nativeQuery = true)
    List<Avtale> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterAvslatt(
            @Param("tilskuddsperiodestatus") String tilskuddsperiodestatus,
            @Param("navEnheter") Set<String> navEnheter,
            @Param("tiltakstype") String tiltakstype);

}

