package no.nav.tag.tiltaksgjennomforing.arena.repository;

import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakgjennomforing;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakskode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArenaTiltakgjennomforingRepository extends JpaRepository<ArenaTiltakgjennomforing, Integer> {
    List<ArenaTiltakgjennomforing> findByArbgivIdArrangor(Integer arbgivIdArrangor);

    @Query("""
        SELECT aoa.virksomhetsnummer
        FROM ArenaTiltakgjennomforing atg, ArenaTiltakdeltaker atd, ArenaOrdsArbeidsgiver aoa
        WHERE atg.arbgivIdArrangor = aoa.arbgivIdArrangor
          AND atg.tiltakgjennomforingId = atd.tiltakgjennomforingId
          AND atd.deltakerstatuskode IN ('GJENN', 'TILBUD')
          AND atg.tiltakskode = :tiltakskode
    """)
    List<String> findVirksomhetsnummerByTiltakskode(ArenaTiltakskode tiltakskode, Pageable pageable);
}
