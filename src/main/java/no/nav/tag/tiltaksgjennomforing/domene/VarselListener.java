package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.domene.events.GodkjentAvDeltaker;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VarselListener {
    private final VarselService varselService;

    @EventListener
    public void avtaleGodkjent(GodkjentAvDeltaker godkjentAvDeltaker) {
        varselService.sendVarsel();
    }
}
