package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AvtaleFactory {
    public static Avtale nyAvtale(OpprettAvtale opprettAvtale, NavIdent navIdent) {
        return new Avtale(opprettAvtale.getDeltakerFnr(), opprettAvtale.getBedriftNr(), navIdent, opprettAvtale.getTiltakstype());
    }

}
