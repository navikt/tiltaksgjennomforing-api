package no.nav.tag.tiltaksgjennomforing.domene.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;

@Value
public class GodkjenningerOpphevetAvArbeidsgiver {
    private final Avtale avtale;
}
