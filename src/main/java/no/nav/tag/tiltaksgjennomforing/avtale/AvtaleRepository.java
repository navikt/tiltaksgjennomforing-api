package no.nav.tag.tiltaksgjennomforing.avtale;

import io.micrometer.core.annotation.Timed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;


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
    @Query("""
        SELECT a
        FROM Avtale a
        WHERE a.bedriftNr IN :bedriftNrList
          AND a.feilregistrert = false
          AND (a.gjeldendeInnhold.godkjentAvVeileder IS NULL OR a.gjeldendeInnhold.sluttDato > :dato12UkerFraIdag)
        """)
    Page<Avtale> findAllByBedriftNr(
        @Param("bedriftNrList") Set<BedriftNr> bedriftNrList,
        @Param("dato12UkerFraIdag") LocalDate dato12UkerFraIdag,
        Pageable pageable
    );

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Query("""
        SELECT a
        FROM Avtale a
        WHERE a.bedriftNr IN :bedriftNrList
          AND a.tiltakstype = :tiltakstype
          AND a.feilregistrert = false
          AND (a.gjeldendeInnhold.godkjentAvVeileder IS NULL OR a.gjeldendeInnhold.sluttDato > :dato12UkerFraIdag)
        """)
    Page<Avtale> findAllByBedriftNrInAndTiltakstype(
        @Param("bedriftNrList") Set<BedriftNr> bedriftNrList,
        @Param("tiltakstype") Tiltakstype tiltakstype,
        @Param("dato12UkerFraIdag") LocalDate dato12UkerFraIdag,
        Pageable pageable
    );

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByDeltakerFnrAndFeilregistrertIsFalse(Fnr deltakerFnr, Pageable pageable);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    List<Avtale> findAllByDeltakerFnrAndFeilregistrertIsFalse(Fnr deltakerFnr);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Query("""
        SELECT a
        FROM Avtale a
        WHERE a.mentorFnr = :mentorFnr
          AND a.feilregistrert = false
          AND (a.gjeldendeInnhold.godkjentAvVeileder IS NULL OR a.gjeldendeInnhold.sluttDato > :dato12UkerFraIdag)
        """)
    Page<Avtale> findAllByMentorFnr(Fnr mentorFnr, LocalDate dato12UkerFraIdag, Pageable pageable);

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

    @Query(value = """
        SELECT a
        FROM Avtale a
        WHERE a.tiltakstype IN ('MIDLERTIDIG_LONNSTILSKUDD', 'VARIG_LONNSTILSKUDD', 'SOMMERJOBB', 'VTAO')
          AND a.status IN ('GJENNOMFØRES', 'KLAR_FOR_OPPSTART')
          AND (a.gjeldendeTilskuddsperiode IS NULL OR EXISTS (
              SELECT tp
              FROM TilskuddPeriode tp
              WHERE tp.avtale = a
                AND tp.startDato > a.gjeldendeTilskuddsperiode.sluttDato
                AND tp.startDato <= current_date + 3 month
                AND tp.status = 'UBEHANDLET'
          ))
    """)
    Slice<Avtale> finnAvtaleMedAktiveTilskuddsperioder(Pageable pageable);

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
        WITH AvtalerSomHarReturnertSomKanBehandles AS (
            SELECT a2.id as avtaleId,
            EXISTS (
                SELECT tp.id
                FROM TilskuddPeriode tp
                WHERE tp.avtale = a2
                 AND a2.gjeldendeTilskuddsperiode.status = 'UBEHANDLET'
                 AND a2.gjeldendeTilskuddsperiode.løpenummer = tp.løpenummer
                 AND tp.status = 'AVSLÅTT'
            ) as avtaleHarReturnertSomKanBehandles
            FROM Avtale a2
        )
        SELECT a.id AS id,
               a.avtaleNr AS avtaleNr,
               a.tiltakstype AS tiltakstype,
               (CASE
                   WHEN a.gjeldendeTilskuddsperiode.status = 'GODKJENT' THEN a.gjeldendeTilskuddsperiode.godkjentAvNavIdent
                   WHEN a.gjeldendeTilskuddsperiode.status = 'AVSLÅTT' THEN a.gjeldendeTilskuddsperiode.avslåttAvNavIdent
                   ELSE a.veilederNavIdent
               END) AS veilederNavIdent,
               a.gjeldendeInnhold.deltakerFornavn AS deltakerFornavn,
               a.opprettetTidspunkt AS opprettetTidspunkt,
               a.sistEndret AS sistEndret,
               a.gjeldendeInnhold.deltakerEtternavn AS deltakerEtternavn,
               a.deltakerFnr AS deltakerFnr,
               a.gjeldendeInnhold.bedriftNavn AS bedriftNavn,
               a.bedriftNr AS bedriftNr,
               a.gjeldendeTilskuddsperiode.startDato AS startDato,
               a.gjeldendeTilskuddsperiode.status AS status,
               (SELECT count(tp.id)
                FROM TilskuddPeriode tp
                WHERE tp.avtale = a
                  AND tp.startDato >= a.gjeldendeTilskuddsperiode.startDato
                  AND tp.startDato <= current_date + 3 month
                  AND (a.kreverOppfolgingFom IS NULL OR tp.startDato < a.kreverOppfolgingFom + 2 month)
                  AND tp.status = a.gjeldendeTilskuddsperiode.status
               ) AS antallUbehandlet,
               a.status AS avtaleStatus,
               a.enhetOppfolging AS enhetOppfolging,
               ashrskb.avtaleHarReturnertSomKanBehandles AS harReturnertSomKanBehandles
        FROM Avtale a
        LEFT JOIN AvtalerSomHarReturnertSomKanBehandles ashrskb ON a.id = ashrskb.avtaleId
        WHERE a.gjeldendeInnhold.godkjentAvVeileder IS NOT NULL
          AND a.feilregistrert IS FALSE
          AND a.tiltakstype IN (:tiltakstype)
          AND a.enhetOppfolging IN (:navEnheter)
          AND (:tilskuddsperiodestatus IS NULL OR a.gjeldendeTilskuddsperiode.status = :tilskuddsperiodestatus)
          AND (:avtaleNr IS NULL OR a.avtaleNr = :avtaleNr)
          AND (:bedriftNr IS NULL OR a.bedriftNr = :bedriftNr)
          AND (:harReturnertSomKanBehandles IS FALSE OR ashrskb.avtaleHarReturnertSomKanBehandles = :harReturnertSomKanBehandles)
          AND (:avtaleStatus IS NULL OR a.status IN :avtaleStatus)
    """)
    Page<BeslutterOversiktEntity> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheter(
        TilskuddPeriodeStatus tilskuddsperiodestatus,
        Set<Tiltakstype> tiltakstype,
        Set<Status> avtaleStatus,
        Set<String> navEnheter,
        BedriftNr bedriftNr,
        Integer avtaleNr,
        boolean harReturnertSomKanBehandles,
        Pageable pageable
    );

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
            LEFT OUTER JOIN TilskuddPeriode t ON t.id = a.gjeldendeTilskuddsperiode.id
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

    Stream<Avtale> streamAllByStatusIn(Set<Status> avtalekravStatuser);

    List<Avtale> findAllByEnhetOppfolging(String enhetOppfolging);
}
