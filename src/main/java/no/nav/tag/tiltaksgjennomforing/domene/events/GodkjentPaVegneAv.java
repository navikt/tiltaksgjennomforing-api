package no.nav.tag.tiltaksgjennomforing.domene.events;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.Identifikator;

@Data
public class GodkjentPaVegneAv {
    private final Avtale avtale;
    private final Identifikator utfortAv;
}