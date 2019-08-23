package no.nav.tag.tiltaksgjennomforing.domene.prosess;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.domene.events.GodkjentAvVeileder;
import no.nav.tag.tiltaksgjennomforing.integrasjon.kafka.avtale.JournalfoeringService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProsessLytter {
    private final JournalfoeringService journalfoeringService;

    @EventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) {
        journalfoeringService.sendTilJournalfoeringHvisGodkjentAvAlle(event.getAvtale());
    }
}
