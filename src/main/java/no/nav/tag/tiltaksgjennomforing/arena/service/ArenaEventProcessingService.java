package no.nav.tag.tiltaksgjennomforing.arena.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;

public interface ArenaEventProcessingService {

    void process(ArenaEvent arenaEvent) throws JsonProcessingException;

}
