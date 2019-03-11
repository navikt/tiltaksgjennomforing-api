package no.nav.tag.tiltaksgjennomforing.domene.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.Avtalerolle;

@Value
public class GodkjenningerOpphevet {
    private final Avtale avtale;
    private final Avtalerolle utfortAv;
}
