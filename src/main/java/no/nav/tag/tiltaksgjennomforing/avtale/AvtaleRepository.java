package no.nav.tag.tiltaksgjennomforing.avtale;

import io.micrometer.core.annotation.Timed;

import java.sql.Date;
import java.time.LocalDate;
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
    Optional<Avtale> findByAvtaleNr(Integer avtaleNr);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    @Override
    List<Avtale> findAllById(Iterable<UUID> ids);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    List<Avtale> findAllByBedriftNr(BedriftNr bedriftNr);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByBedriftNrIn(Set<BedriftNr> bedriftNrList, Pageable pageable);
    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByBedriftNr(BedriftNr bedriftNr, Pageable pageable);
    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByBedriftNrAndTiltakstype(BedriftNr bedriftNr, Tiltakstype tiltakstype, Pageable pageable);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByBedriftNrInAndTiltakstype(Set<BedriftNr> bedriftNrList, Tiltakstype tiltakstype, Pageable pageable);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByDeltakerFnr(Fnr deltakerFnr, Pageable pageable);
    Page<Avtale> findAllByDeltakerFnrAndTiltakstype(Fnr deltakerFnr, Tiltakstype tiltakstype, Pageable pageable);
    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByVeilederNavIdent(NavIdent veilederNavIdent, Pageable pageable);
    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByVeilederNavIdentAndTiltakstype(NavIdent veilederNavIdent, Tiltakstype tiltakstype, Pageable pageable);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    List<Avtale> findAllByDeltakerFnr(Fnr deltakerFnr);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByMentorFnr(Fnr mentorFnr, Pageable pageable);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByVeilederNavIdentIsNullAndEnhetGeografiskOrVeilederNavIdentIsNullAndEnhetOppfolging(String enhetGeografisk, String enhetOppfolging, Pageable pageable);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByVeilederNavIdentIsNullAndEnhetGeografiskAndTiltakstypeOrVeilederNavIdentIsNullAndEnhetOppfolgingAndTiltakstype(String enhetGeografisk, Tiltakstype tiltakstype, String enhetOppfolging, Tiltakstype tiltakstype2, Pageable pageable);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByEnhetGeografiskOrEnhetOppfolging(String enhetGeografisk, String enhetOppfolging, Pageable pageable);

    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByEnhetGeografiskAndTiltakstypeOrEnhetOppfolgingAndTiltakstype(String enhetGeografisk, Tiltakstype tiltakstype, String enhetOppfolging, Tiltakstype tiltakstype2, Pageable pageable);
    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByAvtaleNr(Integer avtaleNr, Pageable pageable);
    @Timed(percentiles = {0.5d, 0.75d, 0.9d, 0.99d, 0.999d})
    Page<Avtale> findAllByAvtaleNrAndTiltakstype(Integer avtaleNr, Tiltakstype tiltakstype, Pageable pageable);

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

    @Query(value = "SELECT a.id as id, a.veilederNavIdent as veilederNavIdent, a.gjeldendeInnhold.deltakerFornavn as deltakerFornavn, " +
            "a.opprettetTidspunkt as opprettetTidspunkt, a.sistEndret as sistEndret, a.gjeldendeInnhold.deltakerEtternavn as deltakerEtternavn, " +
            "a.deltakerFnr as deltakerFnr, a.gjeldendeInnhold.bedriftNavn as bedriftNavn, a.bedriftNr as bedriftNr, min(t.startDato) as startDato, " +
            " t.status as status, count(t.id) as antallUbehandlet " +
            "from Avtale a " +
            "left join AvtaleInnhold i on i.id = a.gjeldendeInnhold.id " +
            "left join TilskuddPeriode t on (t.avtale.id = a.id and t.status = :tilskuddsperiodestatus and t.startDato <= :decisiondate) " +
            "where a.gjeldendeInnhold.godkjentAvVeileder is not null " +
            "and a.tiltakstype in (:tiltakstype) " +
            "and exists (select distinct p.avtale.id, p.status, p.løpenummer, p.startDato from TilskuddPeriode p where p.avtale.id = a.id " +
            "and ((:tilskuddsperiodestatus = p.status and p.startDato <= :decisiondate) or (:tilskuddsperiodestatus = p.status AND p.løpenummer = 1))) " +
            "and a.enhetOppfolging IN (:navEnheter) AND (:bedriftNr is null or cast(a.bedriftNr as text) = :bedriftNr) " +
            "GROUP BY a.id, a.gjeldendeInnhold.deltakerFornavn, a.gjeldendeInnhold.deltakerEtternavn, a.veilederNavIdent, a.gjeldendeInnhold.bedriftNavn, status ",
            nativeQuery = false)
    Page<BeslutterOversiktDTO> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheter(
            @Param("tilskuddsperiodestatus") TilskuddPeriodeStatus tilskuddsperiodestatus,
            @Param("decisiondate") LocalDate decisiondate,
            @Param("tiltakstype") Set<Tiltakstype> tiltakstype,
            @Param("navEnheter") Set<String> navEnheter,
            @Param("bedriftNr") String bedriftNr,
            Pageable pageable);

    @Query(value = "select a from Avtale a " +
            "left join AvtaleInnhold i on i.id = a.gjeldendeInnhold.id " +
            "where (:avtaleNr is null or a.avtaleNr = :avtaleNr) and (:veilederNavIdent is null or a.veilederNavIdent = :veilederNavIdent) " +
            "and (:deltakerFnr is null or a.deltakerFnr = :deltakerFnr) and (:bedriftNr is null or cast(a.bedriftNr as text) in (:bedriftNr)) " +
            "and (:tiltakstype is null or a.tiltakstype = :tiltakstype)",
            nativeQuery = false)
    Page<Object> finnAvtalerForInnloggetVeilederHvorStatusIkkeErSatt(
            @Param("avtaleNr") Integer avtaleNr,
            @Param("veilederNavIdent") NavIdent veilederNavIdent,
            @Param("deltakerFnr") Fnr deltakerFnr,
            @Param("bedriftNr") List<String> bedriftNr,
            @Param("tiltakstype") Tiltakstype tiltakstype,
            Pageable pageable
    );

    @Query(value = "select a from Avtale a " +
            "left join AvtaleInnhold i on i.id = a.gjeldendeInnhold.id " +
            "where (:avtaleNr is null or a.avtaleNr = :avtaleNr) and (:veilederNavIdent is null or a.veilederNavIdent = :veilederNavIdent) " +
            "and (:deltakerFnr is null or a.deltakerFnr = :deltakerFnr) and (:bedriftNr is null or cast(a.bedriftNr as text) in (:bedriftNr)) " +
            "and (:tiltakstype is null or a.tiltakstype = :tiltakstype) and a.annullertTidspunkt is not null",
            nativeQuery = false)
    Page<Object> finnAvtalerForInnloggetVeilederHvorStatusErAnnullert(
            @Param("avtaleNr") Integer avtaleNr,
            @Param("veilederNavIdent") NavIdent veilederNavIdent,
            @Param("deltakerFnr") Fnr deltakerFnr,
            @Param("bedriftNr") List<String> bedriftNr,
            @Param("tiltakstype") Tiltakstype tiltakstype,
            Pageable pageable
    );

    @Query(value = "select a from Avtale a " +
            "left join AvtaleInnhold i on i.id = a.gjeldendeInnhold.id " +
            "where (:avtaleNr is null or a.avtaleNr = :avtaleNr) and (:veilederNavIdent is null or a.veilederNavIdent = :veilederNavIdent) " +
            "and (:deltakerFnr is null or a.deltakerFnr = :deltakerFnr) and (:bedriftNr is null or cast(a.bedriftNr as text) in (:bedriftNr)) " +
            "and (:tiltakstype is null or a.tiltakstype = :tiltakstype) and a.avbrutt is true",
            nativeQuery = false)
    Page<Object> finnAvtalerForInnloggetVeilederHvorStatusErAvbrutt(
            @Param("avtaleNr") Integer avtaleNr,
            @Param("veilederNavIdent") NavIdent veilederNavIdent,
            @Param("deltakerFnr") Fnr deltakerFnr,
            @Param("bedriftNr") List<String> bedriftNr,
            @Param("tiltakstype") Tiltakstype tiltakstype,
            Pageable pageable
    );

    @Query(value = "select a from Avtale a " +
            "left join AvtaleInnhold i on i.id = a.gjeldendeInnhold.id " +
            "where (:avtaleNr is null or a.avtaleNr = :avtaleNr) and (:veilederNavIdent is null or a.veilederNavIdent = :veilederNavIdent) " +
            "and (:deltakerFnr is null or a.deltakerFnr = :deltakerFnr) and (:bedriftNr is null or cast(a.bedriftNr as text) in (:bedriftNr)) " +
            "and (:tiltakstype is null or a.tiltakstype = :tiltakstype) and (a.gjeldendeInnhold.sluttDato <= :decisiondate " +
            "and a.gjeldendeInnhold.avtaleInngått is not null)",
            nativeQuery = false)
    Page<Object> finnAvtalerForInnloggetVeilederHvorStatusErAvsluttet(
            @Param("avtaleNr") Integer avtaleNr,
            @Param("veilederNavIdent") NavIdent veilederNavIdent,
            @Param("deltakerFnr") Fnr deltakerFnr,
            @Param("bedriftNr") List<String> bedriftNr,
            @Param("tiltakstype") Tiltakstype tiltakstype,
            @Param("decisiondate") LocalDate decisiondate,
            Pageable pageable
    );

    @Query(value = "select a from Avtale a " +
            "left join AvtaleInnhold i on i.id = a.gjeldendeInnhold.id " +
            "where (:avtaleNr is null or a.avtaleNr = :avtaleNr) and (:veilederNavIdent is null or a.veilederNavIdent = :veilederNavIdent) " +
            "and (:deltakerFnr is null or a.deltakerFnr = :deltakerFnr) and (:bedriftNr is null or cast(a.bedriftNr as text) in (:bedriftNr)) " +
            "and (:tiltakstype is null or a.tiltakstype = :tiltakstype) and (a.gjeldendeInnhold.startDato <= :decisiondate " +
            "and a.gjeldendeInnhold.avtaleInngått is not null)",
            nativeQuery = false)
    Page<Object> finnAvtalerForInnloggetVeilederHvorStatusErGjennomfores(
            @Param("avtaleNr") Integer avtaleNr,
            @Param("veilederNavIdent") NavIdent veilederNavIdent,
            @Param("deltakerFnr") Fnr deltakerFnr,
            @Param("bedriftNr") List<String> bedriftNr,
            @Param("tiltakstype") Tiltakstype tiltakstype,
            @Param("decisiondate") LocalDate decisiondate,
            Pageable pageable
    );

    @Query(value = "select a from Avtale a " +
            "left join AvtaleInnhold i on i.id = a.gjeldendeInnhold.id " +
            "where (:avtaleNr is null or a.avtaleNr = :avtaleNr) and (:veilederNavIdent is null or a.veilederNavIdent = :veilederNavIdent) " +
            "and (:deltakerFnr is null or a.deltakerFnr = :deltakerFnr) and (:bedriftNr is null or cast(a.bedriftNr as text) in (:bedriftNr)) " +
            "and (:tiltakstype is null or a.tiltakstype = :tiltakstype) and a.gjeldendeInnhold.avtaleInngått is not null",
            nativeQuery = false)
    Page<Object> finnAvtalerForInnloggetVeilederHvorStatusErKlarForOppstart(
            @Param("avtaleNr") Integer avtaleNr,
            @Param("veilederNavIdent") NavIdent veilederNavIdent,
            @Param("deltakerFnr") Fnr deltakerFnr,
            @Param("bedriftNr") List<String> bedriftNr,
            @Param("tiltakstype") Tiltakstype tiltakstype,
            Pageable pageable
    );

    @Query(value = "select a from Avtale a " +
            "left join AvtaleInnhold i on i.id = a.gjeldendeInnhold.id " +
            "where (:avtaleNr is null or a.avtaleNr = :avtaleNr) and (:veilederNavIdent is null or a.veilederNavIdent = :veilederNavIdent) " +
            "and (:deltakerFnr is null or a.deltakerFnr = :deltakerFnr) and (:bedriftNr is null or cast(a.bedriftNr as text) in (:bedriftNr)) " +
            "and (" +
            "a.gjeldendeInnhold.deltakerFornavn is not null and a.gjeldendeInnhold.deltakerEtternavn is not null and a.gjeldendeInnhold.deltakerTlf is not null and " +
            "a.gjeldendeInnhold.bedriftNavn is not null and a.gjeldendeInnhold.arbeidsgiverFornavn is not null and a.gjeldendeInnhold.arbeidsgiverEtternavn is not null " +
            "and a.gjeldendeInnhold.arbeidsgiverTlf is not null and a.gjeldendeInnhold.veilederFornavn is not null and a.gjeldendeInnhold.veilederEtternavn is not null" +
            ") " +
            "and (:tiltakstype  = 'MIDLERTIDIG_LONNSTILSKUDD')",
            nativeQuery = false)
    Page<Object> finnAvtalerForInnloggetVeilederHvorStatusErManglerGodkjenning(
            @Param("avtaleNr") Integer avtaleNr,
            @Param("veilederNavIdent") NavIdent veilederNavIdent,
            @Param("deltakerFnr") Fnr deltakerFnr,
            @Param("bedriftNr") List<String> bedriftNr,
            @Param("tiltakstype") Tiltakstype tiltakstype,
            Pageable pageable
    );


}