package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Value;
import no.bekk.bekkopen.banking.KidnummerValidator;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

@Value
public class EndreKidOgKontonummer {
    String arbeidsgiverKontonummer;
    String arbeidsgiverKid;

    public boolean harMangler() {
        return Utils.erNoenTomme(arbeidsgiverKontonummer);
    }

    public boolean harGyldigKid() {
        return arbeidsgiverKid == null || KidnummerValidator.isValid(arbeidsgiverKid);
    }
}
