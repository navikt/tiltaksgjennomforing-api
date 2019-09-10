package no.nav.tag.tiltaksgjennomforing.domene.events;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.Identifikator;

@Value
public class GodkjentAvArbeidsgiver implements AvtaleGodkjent {
    private final Avtale avtale;
    private final Identifikator utfortAv;
}
