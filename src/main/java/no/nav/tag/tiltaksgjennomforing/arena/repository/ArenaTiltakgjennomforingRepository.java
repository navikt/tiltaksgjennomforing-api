package no.nav.tag.tiltaksgjennomforing.arena.repository;

import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakgjennomforing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArenaTiltakgjennomforingRepository extends JpaRepository<ArenaTiltakgjennomforing, Integer> {
}
