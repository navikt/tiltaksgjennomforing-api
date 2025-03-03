package no.nav.tag.tiltaksgjennomforing.arena.repository;

import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEventStatus;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArenaEventRepository extends JpaRepository<ArenaEvent, UUID> {

    Optional<ArenaEvent> findByArenaIdAndArenaTable(String arenaId, String arenaTable);

    @Query("""
        SELECT ae
        FROM ArenaEvent ae
        WHERE ae.status = :status
    """)
    List<ArenaEvent> findByStatus(ArenaEventStatus status, Limit limit);

    @Query("""
        SELECT ae
        FROM ArenaEvent ae
        WHERE ae.status = :status AND ae.arenaTable = :arenaTable
    """)
    List<ArenaEvent> findByStatusAndArenaTable(ArenaEventStatus status, String arenaTable, Limit limit);

    long countByStatus(ArenaEventStatus status);
}
