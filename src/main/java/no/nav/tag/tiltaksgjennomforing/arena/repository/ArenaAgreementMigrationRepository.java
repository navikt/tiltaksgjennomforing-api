package no.nav.tag.tiltaksgjennomforing.arena.repository;

import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementAggregate;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementMigration;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

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
                atg.regDato
            )
        FROM ArenaTiltakgjennomforing atg
        LEFT JOIN ArenaTiltakdeltaker atd ON atd.tiltakgjennomforingId = atg.tiltakgjennomforingId
        LEFT JOIN ArenaOrdsFnr aof ON atd.personId = aof.personId
        LEFT JOIN ArenaOrdsArbeidsgiver aoa ON atg.arbgivIdArrangor = aoa.arbgivIdArrangor
        WHERE atg.tiltakgjennomforingId NOT IN (SELECT tiltakgjennomforingId FROM ArenaAgreementMigration)
        ORDER BY atd.tiltakgjennomforingId LIMIT 5000
    """)
    List<ArenaAgreementAggregate> findMigrationAgreementAggregates();

    @Query("""
        SELECT a
        FROM Avtale a
        WHERE a.tiltakstype = 'ARBEIDSTRENING'
          AND a.status = 'GJENNOMFØRES'
          AND a.id NOT IN (SELECT avtaleId FROM ArenaAgreementMigration)
    """)
    List<Avtale> findAgreementsForCleanUp(Limit limit);

}
