package no.nav.tag.tiltaksgjennomforing.avtale;

import io.micrometer.core.annotation.Timed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Override
    Avtale save(Avtale entity);

    @Query(
        value = """
            SELECT avtale.* FROM avtale
            LEFT JOIN avtale_innhold
            ON avtale.ID = avtale_innhold.AVTALE
            WHERE :deltakerFnr = avtale.deltaker_fnr AND
                  avtale.annullert_tidspunkt IS NULL AND
                  avtale.avbrutt IS FALSE AND
                  avtale.slettemerket IS FALSE AND
                  (
                      (
                          CAST(:startDato AS DATE) IS NOT NULL AND avtale_innhold.start_dato IS NOT NULL AND avtale_innhold.slutt_dato IS NOT NULL AND
                          (CAST(:startDato AS DATE) >= avtale_innhold.start_dato AND CAST(:startDato AS DATE) <= avtale_innhold.slutt_dato)
                      )
                      OR AVTALE_INNHOLD.godkjent_av_veileder IS NULL
                  )
        """,
        nativeQuery = true
    )
    List<Avtale> finnAvtalerSomOverlapperForDeltakerVedOpprettelseAvAvtale(
            @Param("deltakerFnr") String deltakerFnr,
            @Param("startDato") Date startDato
    );

    @Query(
        value = """
            SELECT avtale.* FROM avtale LEFT JOIN avtale_innhold
            ON avtale.ID = avtale_innhold.AVTALE
            WHERE :deltakerFnr = AVTALE.deltaker_fnr AND
                  (:avtaleId IS NOT NULL AND :avtaleId NOT LIKE CAST(avtale.id as text)) AND
                  AVTALE.annullert_tidspunkt is null and
                  AVTALE.avbrutt is false and
                  AVTALE.slettemerket is false and
                  (
                      (
                          CAST(:startDato AS DATE) IS NOT NULL AND avtale_innhold.start_dato IS NOT NULL AND avtale_innhold.slutt_dato IS NOT NULL AND
                          (CAST(:startDato AS DATE) >= avtale_innhold.start_dato AND CAST(:startDato AS DATE) <= avtale_innhold.slutt_dato)
                      )
                      OR
                      (
                          CAST(:sluttDato AS DATE) IS NOT NULL AND avtale_innhold.start_dato IS NOT NULL AND avtale_innhold.slutt_dato IS NOT NULL AND
                          (CAST(:sluttDato AS DATE) >= avtale_innhold.start_dato AND CAST(:sluttDato AS DATE) <= avtale_innhold.slutt_dato)
                      )
                      OR AVTALE_INNHOLD.godkjent_av_veileder is null
                  )
        """,
        nativeQuery = true
    )
    List<Avtale> finnAvtalerSomOverlapperForDeltakerVedGodkjenningAvAvtale(
            @Param("deltakerFnr") String deltakerFnr,
            @Param("avtaleId") String avtaleId,
            @Param("startDato") Date startDato,
            @Param("sluttDato") Date sluttDato
    );

    @Query(value = """
        SELECT a.id as id,
               a.avtaleNr as avtaleNr,
               a.tiltakstype as tiltakstype,
               a.veilederNavIdent as veilederNavIdent,
               a.gjeldendeInnhold.deltakerFornavn as deltakerFornavn,
               a.opprettetTidspunkt as opprettetTidspunkt,
               a.sistEndret as sistEndret,
               a.gjeldendeInnhold.deltakerEtternavn as deltakerEtternavn,
               a.deltakerFnr as deltakerFnr,
               a.gjeldendeInnhold.bedriftNavn as bedriftNavn,
               a.bedriftNr as bedriftNr,
               min(t.startDato) as startDato,
               t.status,
               count(t.id) as antallUbehandlet
        FROM Avtale a
        LEFT JOIN AvtaleInnhold i ON i.id = a.gjeldendeInnhold.id
        LEFT JOIN TilskuddPeriode t ON (t.avtale.id = a.id AND t.status = :tilskuddsperiodestatus AND t.startDato <= :decisiondate)
        WHERE a.gjeldendeInnhold.godkjentAvVeileder IS NOT NULL AND
              a.tiltakstype in (:tiltakstype) AND
              EXISTS (SELECT DISTINCT p.avtale.id, p.status, p.løpenummer, p.startDato FROM TilskuddPeriode p WHERE p.avtale.id = a.id AND
              ((:tilskuddsperiodestatus = p.status AND p.startDato <= :decisiondate) OR (:tilskuddsperiodestatus = p.status AND p.løpenummer = 1))) AND
              a.enhetOppfolging IN (:navEnheter) AND (:avtaleNr IS NULL OR a.avtaleNr = :avtaleNr) AND (:bedriftNr IS NULL OR cast(a.bedriftNr AS text) = :bedriftNr)
        GROUP BY a.id, a.gjeldendeInnhold.deltakerFornavn, a.gjeldendeInnhold.deltakerEtternavn, a.veilederNavIdent, a.gjeldendeInnhold.bedriftNavn, t.status
    """)
    Page<BeslutterOversiktDTO> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheter(
            @Param("tilskuddsperiodestatus") TilskuddPeriodeStatus tilskuddsperiodestatus,
            @Param("decisiondate") LocalDate decisiondate,
            @Param("tiltakstype") Set<Tiltakstype> tiltakstype,
            @Param("navEnheter") Set<String> navEnheter,
            @Param("bedriftNr") String bedriftNr,
            @Param("avtaleNr") Integer avtaleNr,
            Pageable pageable);

    @Query(
        value = """
            SELECT avtale.*
            FROM avtale, avtale_innhold
            WHERE avtale.gjeldende_innhold_id = avtale_innhold.id
              AND avtale_innhold.avtale_inngått IS NULL
              AND avtale.annullert_tidspunkt IS NULL
              AND avtale.avbrutt IS FALSE
        """,
        nativeQuery = true
    )
    List<Avtale> findAvtalerSomErPabegyntEllerManglerGodkjenning();

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Query(
        value = """
            SELECT a
            FROM Avtale a
            WHERE a.feilregistrert = FALSE AND
                  a.veilederNavIdent = :veilederNavIdent AND
                  (:avtaleNr IS NULL OR a.avtaleNr = :avtaleNr) AND
                  (:tiltakstype IS NULL OR a.tiltakstype = :tiltakstype) AND
                  (:deltakerFnr IS NULL OR a.deltakerFnr = :deltakerFnr) AND
                  (:bedriftNr IS NULL OR a.bedriftNr = :bedriftNr) AND
                  (:enhet IS NULL OR a.enhetGeografisk = :enhet OR a.enhetOppfolging = :enhet) AND
                  (:status IS NULL OR a.status = :status)
        """,
        countQuery = """
            SELECT COUNT(a)
            FROM Avtale a
            WHERE a.feilregistrert = FALSE AND
                  a.veilederNavIdent = :veilederNavIdent AND
                  (:avtaleNr IS NULL OR a.avtaleNr = :avtaleNr) AND
                  (:tiltakstype IS NULL OR a.tiltakstype = :tiltakstype) AND
                  (:deltakerFnr IS NULL OR a.deltakerFnr = :deltakerFnr) AND
                  (:bedriftNr IS NULL OR a.bedriftNr = :bedriftNr) AND
                  (:enhet IS NULL OR a.enhetGeografisk = :enhet OR a.enhetOppfolging = :enhet) AND
                  (:status IS NULL OR a.status = :status)
        """
    )
    Page<Avtale> sokEtterAvtale(
        Integer avtaleNr,
        NavIdent veilederNavIdent,
        Fnr deltakerFnr,
        BedriftNr bedriftNr,
        String enhet,
        Tiltakstype tiltakstype,
        Status status,
        Pageable pageable
    );

}
