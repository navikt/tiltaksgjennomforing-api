package no.nav.tag.tiltaksgjennomforing.domene.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.GamleVerdier;

@Value
public class GodkjenningerOpphevetAvArbeidsgiver {
    private final Avtale avtale;
    private final GamleVerdier gamleVerdier;
}
