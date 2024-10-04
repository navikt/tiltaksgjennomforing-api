package no.nav.tag.tiltaksgjennomforing.arena.repository;

import jakarta.persistence.LockModeType;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementAggregate;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementMigration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArenaAgreementMigrationRepository extends JpaRepository<ArenaAgreementMigration, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT
            new ArenaAgreementAggregate(
                ats.sakId,
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
                atg.prosentDeltid,
                atg.tiltakstatuskode,
                atd.deltakerstatuskode,
                atd.datoFra,
                atd.datoTil,
                ats.regDato
            )
        FROM
            ArenaTiltakssak ats,
            ArenaTiltakgjennomforing atg,
            ArenaTiltakdeltaker atd,
            ArenaOrdsFnr aof,
            ArenaOrdsArbeidsgiver aoa
        WHERE
            ats.sakId = atg.sakId AND
            atd.tiltakgjennomforingId = atg.tiltakgjennomforingId AND
            atd.personId = aof.personId AND
            atg.arbgivIdArrangor = aoa.arbgivIdArrangor AND
            atg.tiltakgjennomforingId NOT IN (SELECT tiltakgjennomforingId FROM ArenaAgreementMigration)
        ORDER BY atd.tiltakgjennomforingId LIMIT 100
    """)
    List<ArenaAgreementAggregate> findMigrationAgreementAggregates();

}
