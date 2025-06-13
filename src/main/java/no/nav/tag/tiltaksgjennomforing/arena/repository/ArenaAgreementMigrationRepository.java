package no.nav.tag.tiltaksgjennomforing.arena.repository;

import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementAggregate;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementErrorCount;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementMigration;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementMigrationCount;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ArenaAgreementMigrationRepository extends JpaRepository<ArenaAgreementMigration, Integer> {

    @Query("""
        SELECT
            new ArenaAgreementAggregate(
                atg.sakId,
                atg.tiltakgjennomforingId,
                atd.tiltakdeltakerId,
                aof.personId,
                aoa.arbgivIdArrangor,
                aoa.virksomhetsnummer,
                aof.fnr,
                atd.antallDagerPrUke,
                atg.datoFra,
                atg.datoTil,
                atg.eksternId,
                atd.eksternId,
                atg.prosentDeltid,
                atg.tiltakstatuskode,
                atd.deltakerstatuskode,
                atd.datoFra,
                atd.datoTil,
                atg.regDato,
                atg.tiltakskode
            )
        FROM ArenaTiltakgjennomforing atg
        FULL OUTER JOIN ArenaTiltakdeltaker atd ON atd.tiltakgjennomforingId = atg.tiltakgjennomforingId
        LEFT JOIN ArenaOrdsFnr aof ON atd.personId = aof.personId
        LEFT JOIN ArenaOrdsArbeidsgiver aoa ON atg.arbgivIdArrangor = aoa.arbgivIdArrangor
        WHERE atg.tiltakskode = :tiltakskode AND (
              atg.tiltakgjennomforingId NOT IN (SELECT tiltakgjennomforingId FROM ArenaAgreementMigration) OR
              atd.tiltakdeltakerId NOT IN (SELECT tiltakdeltakerId FROM ArenaAgreementMigration)
        )
        ORDER BY atd.tiltakgjennomforingId LIMIT 2500
    """)
    List<ArenaAgreementAggregate> findMigrationAgreementAggregates(ArenaTiltakskode tiltakskode);

    @Query("""
        SELECT count(*)
        FROM ArenaTiltakgjennomforing atg
        FULL OUTER JOIN ArenaTiltakdeltaker atd ON atd.tiltakgjennomforingId = atg.tiltakgjennomforingId
        LEFT JOIN ArenaOrdsFnr aof ON atd.personId = aof.personId
        LEFT JOIN ArenaOrdsArbeidsgiver aoa ON atg.arbgivIdArrangor = aoa.arbgivIdArrangor
        WHERE atg.tiltakskode = :tiltakskode AND (
              atg.tiltakgjennomforingId NOT IN (SELECT tiltakgjennomforingId FROM ArenaAgreementMigration) OR
              atd.tiltakdeltakerId NOT IN (SELECT tiltakdeltakerId FROM ArenaAgreementMigration)
        )
    """)
    long countMigrationAgreementAggregates(ArenaTiltakskode tiltakskode);

    @Query("""
        SELECT a
        FROM Avtale a
        WHERE a.tiltakstype = :tiltakstype
          AND a.status = 'GJENNOMFØRES'
          AND a.id NOT IN (SELECT aam.avtaleId FROM ArenaAgreementMigration aam WHERE aam.avtaleId IS NOT NULL)
    """)
    List<Avtale> findAgreementsForCleanUp(Tiltakstype tiltakstype);

    @Query("""
        SELECT distinct aam.tiltakdeltakerId
        FROM ArenaAgreementMigration aam
        WHERE aam.avtaleId = :avtaleId OR aam.eksternId = :avtaleId
    """)
    List<Integer> findTiltakdeltakerIdFromAvtaleId(UUID avtaleId);

    @Query("""
        SELECT new ArenaAgreementMigrationCount(aam.status as status, aam.action as action, COUNT(aam) as count)
        FROM ArenaAgreementMigration aam
        WHERE aam.tiltakstype = :tiltakskode
        GROUP BY aam.status, aam.action
        ORDER BY aam.status, aam.action
    """)
    List<ArenaAgreementMigrationCount> getStatistics(ArenaTiltakskode tiltakskode);

    @Query("""
        SELECT new ArenaAgreementErrorCount(aam.error, count(aam))
        FROM ArenaAgreementMigration aam
        WHERE aam.tiltakstype = :tiltakskode AND aam.status = 'FAILED'
        GROUP BY aam.error
        ORDER BY aam.error
    """)
    List<ArenaAgreementErrorCount> findMigrationErrors(ArenaTiltakskode tiltakskode);

    @Modifying
    @Query("""
        DELETE FROM ArenaAgreementMigration aam WHERE aam.tiltakstype = :tiltakskode and aam.status = 'FAILED'
    """)
    void reset(ArenaTiltakskode tiltakskode);

}
