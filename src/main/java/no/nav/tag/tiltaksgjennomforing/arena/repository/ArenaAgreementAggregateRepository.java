package no.nav.tag.tiltaksgjennomforing.arena.repository;

import jakarta.persistence.LockModeType;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaAgreementAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArenaAgreementAggregateRepository  extends JpaRepository<ArenaAgreementAggregate, Integer> {

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
            atg.arbgivIdArrangor = aoa.arbgivIdArrangor
        ORDER BY random() LIMIT 100
    """)
    List<ArenaAgreementAggregate> findAgreements();

}
