package no.nav.tag.tiltaksgjennomforing.integrasjon;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.domene.Organisasjon;

@Data
public class BrregEnhet {
    private String organisasjonsnummer;
    private String navn;

    public Organisasjon konverterTilDomeneObjekt() {
        return new Organisasjon(new BedriftNr(organisasjonsnummer), navn);
    }
}
