package no.nav.tag.tiltaksgjennomforing.avtale.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;

@Value
public class AvtaleForlenget {
    Avtale avtale;
    NavIdent utførtAv;
}
