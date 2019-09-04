package no.nav.tag.tiltaksgjennomforing.domene.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;

@Value
public class AvbruttAvVeileder {
    private final Avtale avtale;
    private final NavIdent utfortAv;
}
