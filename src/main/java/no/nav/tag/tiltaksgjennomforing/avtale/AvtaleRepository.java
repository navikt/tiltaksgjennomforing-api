package no.nav.tag.tiltaksgjennomforing.avtale;

import io.micrometer.core.annotation.Timed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query(value = """
        SELECT a
        FROM Avtale a
        WHERE a.deltakerFnr = :deltakerFnr AND
              a.annullertTidspunkt IS NULL AND
              a.avbrutt IS FALSE AND
              a.slettemerket IS FALSE AND
              (
                  (
                      :startDato IS NOT NULL AND a.gjeldendeInnhold.startDato IS NOT NULL AND a.gjeldendeInnhold.sluttDato IS NOT NULL AND
                      (:startDato >= a.gjeldendeInnhold.startDato AND :startDato <= a.gjeldendeInnhold.sluttDato)
                  )
                  OR a.gjeldendeInnhold.godkjentAvVeileder IS NULL
              )
    """)
    List<Avtale> finnAvtalerSomOverlapperForDeltakerVedOpprettelseAvAvtale(
        Fnr deltakerFnr,
        LocalDate startDato
    );

    @Query(value = """
        SELECT a
        FROM Avtale a
        WHERE :deltakerFnr = a.deltakerFnr AND
              (:avtaleId IS NOT NULL AND NOT :avtaleId = a.id) AND
              a.annullertTidspunkt IS NULL AND
              a.avbrutt IS FALSE AND
              a.slettemerket IS FALSE AND
              (
                  (
                      :startDato IS NOT NULL AND a.gjeldendeInnhold.startDato IS NOT NULL AND a.gjeldendeInnhold.sluttDato IS NOT NULL AND
                      (:startDato >= a.gjeldendeInnhold.startDato AND :startDato <= a.gjeldendeInnhold.sluttDato)
                  )
                  OR
                  (
                      :sluttDato IS NOT NULL AND a.gjeldendeInnhold.startDato IS NOT NULL AND a.gjeldendeInnhold.sluttDato IS NOT NULL AND
                      (:sluttDato >= a.gjeldendeInnhold.startDato AND :sluttDato <= a.gjeldendeInnhold.sluttDato)
                  )
                  OR a.gjeldendeInnhold.godkjentAvVeileder IS NULL
              )
    """)
    List<Avtale> finnAvtalerSomOverlapperForDeltakerVedGodkjenningAvAvtale(
        Fnr deltakerFnr,
        UUID avtaleId,
        LocalDate startDato,
        LocalDate sluttDato
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
               t.status,
               count(t.id) AS antallUbehandlet
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
        TilskuddPeriodeStatus tilskuddsperiodestatus,
        LocalDate decisiondate,
        Set<Tiltakstype> tiltakstype,
        Set<String> navEnheter,
        String bedriftNr,
        Integer avtaleNr,
        Pageable pageable
    );

    @Query(value = """
        SELECT a
        FROM Avtale a
        WHERE a.gjeldendeInnhold.avtaleInngått IS NULL
          AND a.annullertTidspunkt IS NULL
          AND a.avbrutt IS FALSE
    """)
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
