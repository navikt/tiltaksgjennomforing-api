package no.nav.tag.tiltaksgjennomforing.avtale;

import io.micrometer.core.annotation.Timed;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    List<Avtale> findAllByTiltakstype(Tiltakstype tiltakstype);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    List<Avtale> findAllByTiltakstypeAndGjeldendeInnhold_DatoForRedusertProsentNullAndGjeldendeInnhold_AvtaleInngåttNotNull(Tiltakstype tiltakstype);
    //List<Avtale> findAllByGjeldendeInnhold_SumLønnstilskuddRedusertNullAndGjeldendeInnhold_AvtaleInngåttNotNullAndTiltakstype(Tiltakstype tiltakstype);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Override
    List<Avtale> findAll();

    List<Avtale> findAllByGjeldendeInnhold_AvtaleInngåttNotNull();

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Override
    Avtale save(Avtale entity);

    @Query(value = "SELECT AVTALE.* FROM AVTALE LEFT JOIN AVTALE_INNHOLD " +
            "ON AVTALE.ID = AVTALE_INNHOLD.AVTALE " +
            "WHERE :deltakerFnr = AVTALE.deltaker_fnr and " +
            "AVTALE.annullert_tidspunkt is null and " +
            "AVTALE.avbrutt is false and " +
            "AVTALE.slettemerket is false and " +
            "((CAST(:startDato as date) is not null and AVTALE_INNHOLD.start_dato is not null and AVTALE_INNHOLD.slutt_dato is not null and" +
            " (CAST(:startDato as date) >= AVTALE_INNHOLD.start_dato and CAST(:startDato as date) <= AVTALE_INNHOLD.slutt_dato)) " +
            "or " +
            "AVTALE_INNHOLD.godkjent_av_veileder is null)"
            , nativeQuery = true)
    List<Avtale> finnAvtalerSomOverlapperForDeltakerVedOpprettelseAvAvtale(
            @Param("deltakerFnr") String deltakerFnr,
            @Param("startDato") Date startDato
    );

    @Query(value = "SELECT AVTALE.* FROM AVTALE LEFT JOIN AVTALE_INNHOLD " +
            "ON AVTALE.ID = AVTALE_INNHOLD.AVTALE " +
            "WHERE :deltakerFnr = AVTALE.deltaker_fnr and " +
            "(:avtaleId is not null and :avtaleId NOT LIKE CAST(AVTALE.id as text)) and " +
            "AVTALE.annullert_tidspunkt is null and " +
            "AVTALE.avbrutt is false and " +
            "AVTALE.slettemerket is false and " +
            "((CAST(:startDato as date) is not null and AVTALE_INNHOLD.start_dato is not null and AVTALE_INNHOLD.slutt_dato is not null and" +
            " (CAST(:startDato as date) >= AVTALE_INNHOLD.start_dato and CAST(:startDato as date) <= AVTALE_INNHOLD.slutt_dato)) " +
            "or " +
            "(CAST(:sluttDato as date) is not null and AVTALE_INNHOLD.start_dato is not null and AVTALE_INNHOLD.slutt_dato is not null and " +
            "(CAST(:sluttDato as date) >= AVTALE_INNHOLD.start_dato and CAST(:sluttDato as date) <= AVTALE_INNHOLD.slutt_dato)) " +
            "or " +
            "AVTALE_INNHOLD.godkjent_av_veileder is null)"
            , nativeQuery = true)
    List<Avtale> finnAvtalerSomOverlapperForDeltakerVedGodkjenningAvAvtale(
            @Param("deltakerFnr") String deltakerFnr,
            @Param("avtaleId") String avtaleId,
            @Param("startDato") Date startDato,
            @Param("sluttDato") Date sluttDato
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
            "SELECT cast(AVTALE.ID as varchar) as id, AVTALE.VEILEDER_NAV_IDENT as veilederNavIdent, AVTALE_INNHOLD.DELTAKER_FORNAVN as deltakerFornavn, :tilskuddsperiodestatus as tilskuddsperiodestatus," +
                    "AVTALE_INNHOLD.DELTAKER_ETTERNAVN as deltakerEtternavn, COUNT(TILSKUDD_PERIODE.ID) as antallUbehandlet, AVTALE.DELTAKER_FNR as deltakerFnr, AVTALE_INNHOLD.BEDRIFT_NAVN as bedriftNavn, AVTALE.BEDRIFT_NR as bedriftNr FROM AVTALE " +
                    "LEFT JOIN AVTALE_INNHOLD ON AVTALE_INNHOLD.ID = AVTALE.GJELDENDE_INNHOLD_ID " +
                    "LEFT JOIN TILSKUDD_PERIODE ON (TILSKUDD_PERIODE.AVTALE_ID = AVTALE.ID AND TILSKUDD_PERIODE.STATUS = :tilskuddsperiodestatus AND TILSKUDD_PERIODE.START_DATO <= current_date + CAST(:plussDato as INTEGER )) " +
                    "WHERE AVTALE_INNHOLD.GODKJENT_AV_VEILEDER is not null " +
                    "AND AVTALE.TILTAKSTYPE in (:tiltakstype) " +
                    "AND EXISTS (SELECT avtale_id, status, løpenummer, start_dato FROM TILSKUDD_PERIODE where avtale_id = AVTALE.ID AND " +
                    "(:tilskuddsperiodestatus = status AND start_dato <= current_date + CAST(:plussDato as INTEGER ) OR (:tilskuddsperiodestatus = status and løpenummer = 1))) " +
                    "AND AVTALE.ENHET_OPPFOLGING IN (:navEnheter) " +
                    "AND (:bedriftNr IS NULL OR AVTALE.BEDRIFT_NR in (:bedriftNr)) " +
                    "GROUP BY AVTALE.ID, AVTALE_INNHOLD.DELTAKER_FORNAVN, AVTALE_INNHOLD.DELTAKER_ETTERNAVN, AVTALE.VEILEDER_NAV_IDENT, AVTALE_INNHOLD.BEDRIFT_NAVN", nativeQuery = true)
    Page<AvtaleMinimal> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterUbehandletMinimal(
            @Param("tilskuddsperiodestatus") String tilskuddsperiodestatus,
            @Param("navEnheter") Set<String> navEnheter,
            @Param("plussDato") int plussDato,
            @Param("tiltakstype") Set<String> tiltakstype,
            @Param("bedriftNr") String bedriftNr,
            Pageable pageable);

    @Query(value =
            "SELECT cast(AVTALE.ID as varchar) as id, AVTALE.VEILEDER_NAV_IDENT as veilederNavIdent, AVTALE_INNHOLD.DELTAKER_FORNAVN as deltakerFornavn, :tilskuddsperiodestatus as tilskuddsperiodestatus," +
                    "AVTALE_INNHOLD.DELTAKER_ETTERNAVN as deltakerEtternavn, COUNT(TILSKUDD_PERIODE.ID) as antallUbehandlet, AVTALE.DELTAKER_FNR as deltakerFnr, AVTALE_INNHOLD.BEDRIFT_NAVN as bedriftNavn, AVTALE.BEDRIFT_NR as bedriftNr FROM AVTALE " +
                    "LEFT JOIN AVTALE_INNHOLD ON AVTALE_INNHOLD.ID = AVTALE.GJELDENDE_INNHOLD_ID " +
                    "LEFT JOIN TILSKUDD_PERIODE ON (TILSKUDD_PERIODE.AVTALE_ID = AVTALE.ID AND TILSKUDD_PERIODE.STATUS = :tilskuddsperiodestatus) " +
                    "WHERE AVTALE_INNHOLD.GODKJENT_AV_VEILEDER is not null " +
                    "AND AVTALE.TILTAKSTYPE in (:tiltakstype) " +
                    "AND EXISTS (SELECT avtale_id, status, løpenummer, start_dato FROM TILSKUDD_PERIODE where avtale_id = AVTALE.ID AND :tilskuddsperiodestatus = status " +
                    "AND AVTALE.ENHET_OPPFOLGING IN (:navEnheter)) " +
                    "AND (:bedriftNr IS NULL OR AVTALE.BEDRIFT_NR in (:bedriftNr)) " +
                    "GROUP BY AVTALE.ID, AVTALE_INNHOLD.DELTAKER_FORNAVN, AVTALE_INNHOLD.DELTAKER_ETTERNAVN, AVTALE.VEILEDER_NAV_IDENT, AVTALE_INNHOLD.BEDRIFT_NAVN", nativeQuery = true)
    Page<AvtaleMinimal> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterGodkjentEllerAvslåttMinimal(
            @Param("tilskuddsperiodestatus") String tilskuddsperiodestatus,
            @Param("navEnheter") Set<String> navEnheter,
            @Param("tiltakstype") Set<String> tiltakstype,
            @Param("bedriftNr") String bedriftNr,
            Pageable pageable);

}

