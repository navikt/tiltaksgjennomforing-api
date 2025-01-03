package no.nav.tag.tiltaksgjennomforing.arena.repository;

import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakgjennomforing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArenaTiltakgjennomforingRepository extends JpaRepository<ArenaTiltakgjennomforing, Integer> {
    List<ArenaTiltakgjennomforing> findByArbgivIdArrangor(Integer arbgivIdArrangor);
}
