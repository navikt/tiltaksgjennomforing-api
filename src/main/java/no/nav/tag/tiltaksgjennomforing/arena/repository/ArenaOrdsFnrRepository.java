package no.nav.tag.tiltaksgjennomforing.arena.repository;

import no.nav.tag.tiltaksgjennomforing.arena.models.ords.ArenaOrdsFnr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArenaOrdsFnrRepository extends JpaRepository<ArenaOrdsFnr, Integer> {
    List<ArenaOrdsFnr> findByPersonId(int personId);
}
