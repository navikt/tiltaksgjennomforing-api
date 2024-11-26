package no.nav.tag.tiltaksgjennomforing.arena.repository;

import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArenaEventRepository extends JpaRepository<ArenaEvent, UUID> {

    Optional<ArenaEvent> findByArenaIdAndArenaTable(String arenaId, String arenaTable);

    @Query("SELECT ae FROM ArenaEvent ae WHERE ae.status = 'CREATED' ORDER BY ae.operationTime ASC LIMIT 1000")
    List<ArenaEvent> findNewEventsForProcessing();

    @Query("SELECT ae FROM ArenaEvent ae WHERE ae.status = 'RETRY' ORDER BY ae.retryCount ASC LIMIT 1000")
    List<ArenaEvent> findRetryEventsForProcessing();

}
