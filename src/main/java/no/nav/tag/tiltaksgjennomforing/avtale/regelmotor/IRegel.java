package no.nav.tag.tiltaksgjennomforing.avtale.regelmotor;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;

public interface IRegel
{
    boolean vurder(Avtale avtale);
    String beskrivelse();
}
