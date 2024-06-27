package no.nav.tag.tiltaksgjennomforing.arena.repository;

import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ArenaEventRepository extends JpaRepository<ArenaEvent, UUID> {

    Optional<ArenaEvent> findByArenaIdAndArenaTable(String arenaId, String arenaTable);
}
