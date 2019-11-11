package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class AvtaleFactory {
    public static Avtale nyAvtale(OpprettAvtale opprettAvtale, NavIdent navIdent) {
        // Default er ARBEIDSTRENING om ikke valgt.
        switch (Optional.ofNullable(opprettAvtale.getTiltakstype()).orElse(Tiltakstype.ARBEIDSTRENING)) {
            case ARBEIDSTRENING:
                return new Arbeidstrening(opprettAvtale.getDeltakerFnr(), opprettAvtale.getBedriftNr(), navIdent);
            case VARIG_LONNSTILSKUDD:
                return new VarigLonnstilskudd(opprettAvtale.getDeltakerFnr(), opprettAvtale.getBedriftNr(), navIdent);
            case MIDLERTIDIG_LONNSTILSKUDD:
                return new MidlertidigLonnstilskudd(opprettAvtale.getDeltakerFnr(), opprettAvtale.getBedriftNr(), navIdent);
            default:
                throw new IllegalStateException("Unexpected value: " + opprettAvtale.getTiltakstype());
        }
    }

}
