package no.nav.tag.tiltaksgjennomforing.avtale.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;

@Value
public class GodkjenningerOpphevetAvArbeidsgiver {
    private final Avtale avtale;
    private final GamleVerdier gamleVerdier;
}
