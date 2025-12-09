package no.nav.tag.tiltaksgjennomforing.arena.repository;

import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakdeltaker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArenaTiltakdeltakerRepository extends JpaRepository<ArenaTiltakdeltaker, Integer> {
    List<ArenaTiltakdeltaker> findByPersonId(Integer personId);

    List<ArenaTiltakdeltaker> findByTiltakdeltakerId(Integer tiltakdeltakerId);
}
