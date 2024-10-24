package no.nav.tag.tiltaksgjennomforing.avtale.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import org.w3c.dom.events.Event;

@Value
public class AvtaleForlenget {
    Avtale avtale;
    NavIdent utførtAv;
}
