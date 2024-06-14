package no.nav.tag.tiltaksgjennomforing.arena.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.TiltaksakEndret;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import org.springframework.stereotype.Service;

@Service
public class TiltaksakArenaEventProcessingService implements ArenaEventProcessingService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void process(ArenaEvent arenaEvent) throws JsonProcessingException {
        TiltaksakEndret tiltaksakEndret = this.objectMapper.treeToValue(arenaEvent.getPayload(), TiltaksakEndret.class);
    }
}
