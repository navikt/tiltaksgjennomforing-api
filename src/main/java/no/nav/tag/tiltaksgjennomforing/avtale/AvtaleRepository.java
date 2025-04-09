package no.nav.tag.tiltaksgjennomforing.avtale;

import io.micrometer.core.annotation.Timed;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


public interface AvtaleRepository extends JpaRepository<Avtale, UUID>, JpaSpecificationExecutor {

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Override
    Optional<Avtale> findById(UUID id);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Optional<Avtale> findByAvtaleNr(Integer avtaleNr);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Override
    List<Avtale> findAllById(Iterable<UUID> ids);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    List<Avtale> findAllByBedriftNrAndFeilregistrertIsFalse(BedriftNr bedriftNr);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByBedriftNrInAndFeilregistrertIsFalse(Set<BedriftNr> bedriftNrList, Pageable pageable);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByBedriftNrInAndTiltakstypeAndFeilregistrertIsFalse(Set<BedriftNr> bedriftNrList, Tiltakstype tiltakstype, Pageable pageable);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByDeltakerFnrAndFeilregistrertIsFalse(Fnr deltakerFnr, Pageable pageable);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    List<Avtale> findAllByDeltakerFnrAndFeilregistrertIsFalse(Fnr deltakerFnr);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByMentorFnrAndFeilregistrertIsFalse(Fnr mentorFnr, Pageable pageable);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByAvtaleNrAndFeilregistrertIsFalse(Integer avtaleNr, Pageable pageable);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    List<Avtale> findAllByTiltakstype(Tiltakstype tiltakstype);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    List<Avtale> findAllByTiltakstypeAndGjeldendeInnhold_DatoForRedusertProsentNullAndGjeldendeInnhold_AvtaleInngåttNotNull(Tiltakstype tiltakstype);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Override
    List<Avtale> findAll();

    List<Avtale> findAllByGjeldendeInnhold_AvtaleInngåttNotNull();

    List<Avtale> findAllByTiltakstypeAndGjeldendeInnhold_AvtaleInngåttNotNull(Tiltakstype tiltakstype);

    @Query(value = """
        select a from Avtale a
            where a.tiltakstype in (:tiltakstyper)
            and a.gjeldendeTilskuddsperiode is null
            and (select count(*) from TilskuddPeriode t where t.aktiv = true and t.avtale = a) > 0
    """)
    List<Avtale> finnAvtaleHvorGjeldendeTilskuddsperiodeKanSettes(Set<Tiltakstype> tiltakstyper, Limit limit);

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

    @Query(value = """
        SELECT a.id AS id,
               a.avtaleNr AS avtaleNr,
               a.tiltakstype AS tiltakstype,
               a.veilederNavIdent AS veilederNavIdent,
               a.gjeldendeInnhold.deltakerFornavn AS deltakerFornavn,
               a.opprettetTidspunkt AS opprettetTidspunkt,
               a.sistEndret AS sistEndret,
               a.gjeldendeInnhold.deltakerEtternavn AS deltakerEtternavn,
               a.deltakerFnr AS deltakerFnr,
               a.gjeldendeInnhold.bedriftNavn AS bedriftNavn,
               a.bedriftNr AS bedriftNr,
               min(t.startDato) AS startDato,
               t.status AS status,
               count(t.id) AS antallUbehandlet,
               a.status AS avtaleStatus,
               a.enhetOppfolging AS enhetOppfolging
        FROM Avtale a
        LEFT JOIN AvtaleInnhold i ON i.id = a.gjeldendeInnhold.id
        LEFT JOIN TilskuddPeriode t ON (t.avtale.id = a.id AND t.status = :tilskuddsperiodestatus AND t.startDato <= :decisiondate)
        WHERE a.gjeldendeInnhold.godkjentAvVeileder IS NOT NULL
          AND a.tiltakstype IN (:tiltakstype)
          AND EXISTS (SELECT DISTINCT p.avtale.id, p.status, p.løpenummer, p.startDato FROM TilskuddPeriode p WHERE p.avtale.id = a.id
          AND ((:tilskuddsperiodestatus = p.status AND p.startDato <= :decisiondate) or (:tilskuddsperiodestatus = p.status AND p.løpenummer = 1)))
          AND a.enhetOppfolging IN (:navEnheter) AND (:avtaleNr IS NULL OR a.avtaleNr = :avtaleNr) AND (:bedriftNr IS NULL OR cast(a.bedriftNr AS text) = :bedriftNr)
        GROUP BY a.id, a.gjeldendeInnhold.deltakerFornavn, a.gjeldendeInnhold.deltakerEtternavn, a.veilederNavIdent, a.gjeldendeInnhold.bedriftNavn, t.status
    """)
    Page<BeslutterOversiktEntity> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheter(
            @Param("tilskuddsperiodestatus") TilskuddPeriodeStatus tilskuddsperiodestatus,
            @Param("decisiondate") LocalDate decisiondate,
            @Param("tiltakstype") Set<Tiltakstype> tiltakstype,
            @Param("navEnheter") Set<String> navEnheter,
            @Param("bedriftNr") String bedriftNr,
            @Param("avtaleNr") Integer avtaleNr,
            Pageable pageable);

    @Query("""
        SELECT a
        FROM Avtale a
        WHERE a.status = no.nav.tag.tiltaksgjennomforing.avtale.Status.PÅBEGYNT OR a.status = no.nav.tag.tiltaksgjennomforing.avtale.Status.MANGLER_GODKJENNING
    """)
    List<Avtale> findAvtalerSomErPabegyntEllerManglerGodkjenning();

    @Query("""
        SELECT a
        FROM Avtale a
        WHERE (a.status = no.nav.tag.tiltaksgjennomforing.avtale.Status.KLAR_FOR_OPPSTART AND a.gjeldendeInnhold.startDato <= current_date)
           OR (a.status = no.nav.tag.tiltaksgjennomforing.avtale.Status.GJENNOMFØRES AND a.gjeldendeInnhold.sluttDato < current_date)
    """)
    List<Avtale> findAvtalerForEndringAvStatus();

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Query(
        value = """
            SELECT a
            FROM Avtale a
            WHERE a.feilregistrert = FALSE AND
                  (:ufordelt = FALSE OR a.veilederNavIdent IS NULL) AND
                  (:ufordelt = TRUE OR :veilederNavIdent IS NULL OR a.veilederNavIdent = :veilederNavIdent) AND
                  (:avtaleNr IS NULL OR a.avtaleNr = :avtaleNr) AND
                  (:deltakerFnr IS NULL OR a.deltakerFnr = :deltakerFnr) AND
                  (:bedriftNr IS NULL OR a.bedriftNr = :bedriftNr) AND
                  (:enhet IS NULL OR a.enhetGeografisk = :enhet OR a.enhetOppfolging = :enhet) AND
                  (:tiltakstype IS NULL OR a.tiltakstype = :tiltakstype) AND
                  (:status IS NULL OR a.status = :status)
        """,
        countQuery = """
            SELECT COUNT(a)
            FROM Avtale a
            WHERE a.feilregistrert = FALSE AND
                  (:ufordelt = FALSE OR a.veilederNavIdent IS NULL) AND
                  (:ufordelt = TRUE OR :veilederNavIdent IS NULL OR a.veilederNavIdent = :veilederNavIdent) AND
                  (:avtaleNr IS NULL OR a.avtaleNr = :avtaleNr) AND
                  (:deltakerFnr IS NULL OR a.deltakerFnr = :deltakerFnr) AND
                  (:bedriftNr IS NULL OR a.bedriftNr = :bedriftNr) AND
                  (:enhet IS NULL OR a.enhetGeografisk = :enhet OR a.enhetOppfolging = :enhet) AND
                  (:tiltakstype IS NULL OR a.tiltakstype = :tiltakstype) AND
                  (:status IS NULL OR a.status = :status)
        """
    )
    Page<Avtale> sokEtterAvtale(
        NavIdent veilederNavIdent,
        Integer avtaleNr,
        Fnr deltakerFnr,
        BedriftNr bedriftNr,
        String enhet,
        Tiltakstype tiltakstype,
        Status status,
        boolean ufordelt,
        Pageable pageable
    );

    @Query(value = """
            SELECT a from Avtale a
            where a.status in ("GJENNOMFØRES", "KLAR_FOR_OPPSTART")
            and a.oppfolgingVarselSendt is null
            and a.kreverOppfolgingFom < :date""")
    List<Avtale> finnAvtalerSomSnartSkalFølgesOpp(LocalDate date);

    Page<Avtale> findByStatusIn(Collection<Status> statuses, Pageable pageable);

    @Query("""
        SELECT distinct a.deltakerFnr
        FROM Avtale a
        WHERE a.status IN ('GJENNOMFØRES', 'AVSLUTTET', 'PÅBEGYNT', 'MANGLER_GODKJENNING', 'KLAR_FOR_OPPSTART')
    """)
    Page<Fnr> findDistinctDeltakerFnr(Pageable pageable);

    @Query("""
        SELECT a
        FROM Avtale a
        WHERE a.deltakerFnr = :deltakerFnr
          AND a.status IN ('GJENNOMFØRES', 'AVSLUTTET', 'PÅBEGYNT', 'MANGLER_GODKJENNING', 'KLAR_FOR_OPPSTART')
    """)
    List<Avtale> findByDeltakerFnr(Fnr deltakerFnr);
}
