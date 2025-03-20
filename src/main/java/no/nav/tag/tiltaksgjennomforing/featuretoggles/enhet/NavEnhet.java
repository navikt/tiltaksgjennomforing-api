package no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(of = "verdi")
public class NavEnhet {
    public static final NavEnhet NAV_VIKAFOSSEN = new NavEnhet("2103", "Nav Vikafossen");

    String verdi;
    String navn;

    public boolean equalsEnhetsnummer(String verdi) {
        return this.equals(new NavEnhet(verdi, null));
    }
}
