package no.nav.tag.tiltaksgjennomforing.arena.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEventStatus;

public interface IArenaEventProcessingService {

    ArenaEventStatus process(ArenaEvent arenaEvent) throws JsonProcessingException;

}
