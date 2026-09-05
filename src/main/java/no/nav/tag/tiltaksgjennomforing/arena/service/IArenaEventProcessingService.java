package no.nav.tag.tiltaksgjennomforing.arena.service;

import tools.jackson.core.JacksonException;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEventStatus;

public interface IArenaEventProcessingService {

    ArenaEventStatus process(ArenaEvent arenaEvent) throws JacksonException;

}
