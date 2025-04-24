package no.nav.tag.tiltaksgjennomforing.avtale;

public class BedriftNr extends Identifikator {
    public BedriftNr(String verdi) {
        super(verdi);
    }

    public static BedriftNr av(String verdi) {
        return new BedriftNr(verdi);
    }
}
