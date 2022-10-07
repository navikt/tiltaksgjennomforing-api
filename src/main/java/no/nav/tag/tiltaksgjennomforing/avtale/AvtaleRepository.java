package no.nav.tag.tiltaksgjennomforing.avtale;

import io.micrometer.core.annotation.Timed;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    List<Avtale> findAllByMentorFnr(Fnr mentorFnr);

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


    @Query(value = "SELECT AVTALE.* FROM AVTALE LEFT JOIN AVTALE_INNHOLD " +
            "ON AVTALE.ID = AVTALE_INNHOLD.AVTALE " +
            "WHERE :deltakerFnr = AVTALE.deltaker_fnr and " +
            "(:avtaleId is null or :avtaleId != CAST(AVTALE.id as text)) and " +
            "AVTALE.annullert_tidspunkt is null and " +
            "AVTALE.avbrutt is false and " +
            "AVTALE.slettemerket is false and " +
            "((:startDato is not null and AVTALE_INNHOLD.start_dato is not null and AVTALE_INNHOLD.slutt_dato is not null and" +
            " (:startDato >= AVTALE_INNHOLD.start_dato and :startDato <= AVTALE_INNHOLD.slutt_dato)) " +
            "or " +
            "(:sluttDato is not null and AVTALE_INNHOLD.start_dato is not null and AVTALE_INNHOLD.slutt_dato is not null and " +
            "(:sluttDato >= AVTALE_INNHOLD.start_dato and :sluttDato <= AVTALE_INNHOLD.slutt_dato)) " +
            "or " +
            "AVTALE_INNHOLD.godkjent_av_veileder is null)"
            , nativeQuery = true)
    List<Avtale> finnAvtalerSomOverlapperForDeltaker(
            @Param("deltakerFnr") String deltakerFnr,
            @Param("avtaleId") String avtaleId,
            @Param("startDato") LocalDate startDato,
            @Param("sluttDato") LocalDate sluttDato
    );


@Query(value =
        "SELECT distinct AVTALE.* FROM AVTALE " +
                "LEFT JOIN AVTALE_INNHOLD " +
                "ON AVTALE.ID = AVTALE_INNHOLD.AVTALE " +
                "WHERE AVTALE_INNHOLD.GODKJENT_AV_VEILEDER is not null " +
                "AND EXISTS (SELECT avtale_id, status, løpenummer, start_dato FROM TILSKUDD_PERIODE where avtale_id = AVTALE.ID AND " +
                "(:tilskuddsperiodestatus LIKE 'UBEHANDLET' AND :tilskuddsperiodestatus = status AND " +
                "((start_dato <= current_date + CAST(:plussDato as INTEGER )) OR (løpenummer = 1 AND status LIKE 'UBEHANDLET')))) " +
                "AND (AVTALE.ENHET_OPPFOLGING IN (:navEnheter) OR AVTALE.ENHET_GEOGRAFISK IN (:navEnheter))", nativeQuery = true)
List<Avtale> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterUbehandlet(
        @Param("tilskuddsperiodestatus") String tilskuddsperiodestatus,
        @Param("navEnheter") Set<String> navEnheter,
        @Param("plussDato") int plussDato);

    @Query(value =
            "SELECT distinct AVTALE.* FROM AVTALE " +
                    "LEFT JOIN AVTALE_INNHOLD " +
                    "ON AVTALE.ID = AVTALE_INNHOLD.AVTALE " +
                    "WHERE AVTALE_INNHOLD.GODKJENT_AV_VEILEDER is not null " +
                    "AND EXISTS (SELECT avtale_id, status FROM TILSKUDD_PERIODE where avtale_id = AVTALE.ID AND " +
                    "((:tilskuddsperiodestatus LIKE 'GODKJENT' AND :tilskuddsperiodestatus = status))) " +
                    "AND NOT EXISTS (SELECT avtale_id, status, løpenummer, start_dato FROM TILSKUDD_PERIODE where " +
                    "avtale_id = AVTALE.ID AND status LIKE 'UBEHANDLET' " +
                    "AND ((start_dato <= current_date + CAST(:plussDato AS INTEGER)) OR (løpenummer = 1 AND status LIKE 'UBEHANDLET'))) " +
                    "AND (AVTALE.ENHET_OPPFOLGING IN (:navEnheter) OR AVTALE.ENHET_GEOGRAFISK IN (:navEnheter))", nativeQuery = true)
    List<Avtale> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterGodkjent(
            @Param("tilskuddsperiodestatus") String tilskuddsperiodestatus,
            @Param("navEnheter") Set<String> navEnheter,
            @Param("plussDato") int plussDato);

    @Query(value =
            "SELECT distinct AVTALE.* FROM AVTALE " +
                    "LEFT JOIN AVTALE_INNHOLD " +
                    "ON AVTALE.ID = AVTALE_INNHOLD.AVTALE " +
                    "WHERE AVTALE_INNHOLD.GODKJENT_AV_VEILEDER is not null " +
                    "AND EXISTS (SELECT avtale_id, status, løpenummer, start_dato FROM TILSKUDD_PERIODE where avtale_id = AVTALE.ID AND " +
                    "(:tilskuddsperiodestatus LIKE 'AVSLÅTT' AND :tilskuddsperiodestatus = status) " +
                    "AND ((start_dato <= current_date + CAST(:plussDato as INTEGER)) OR (løpenummer = 1 AND status LIKE 'UBEHANDLET'))) " +
                    "AND (AVTALE.ENHET_OPPFOLGING IN (:navEnheter) OR AVTALE.ENHET_GEOGRAFISK IN (:navEnheter))", nativeQuery = true)
    List<Avtale> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterAvslatt(
            @Param("tilskuddsperiodestatus") String tilskuddsperiodestatus,
            @Param("navEnheter") Set<String> navEnheter,
            @Param("plussDato") int plussDato);

}

