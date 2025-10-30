package no.nav.tag.tiltaksgjennomforing.avtale.regelmotor.sommerjobb;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.regelmotor.IRegel;

public class SommerjobbRegel implements IRegel {
    @Override
    public boolean vurder(Avtale avtale) {
        return !avtale.getDeltakerFnr().erUnder16år();
    }

    @Override
    public String beskrivelse() {
        return "Deltaker er over 16 år";
    }
}
