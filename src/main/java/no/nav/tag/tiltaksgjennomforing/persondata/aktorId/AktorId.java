package no.nav.tag.tiltaksgjennomforing.persondata.aktorId;

import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;


public class AktorId extends Identifikator {
    private AktorId(String verdi) {
        super(verdi);
    }

    public static AktorId av(String verdi) {
        return new AktorId(verdi);
    }
}
