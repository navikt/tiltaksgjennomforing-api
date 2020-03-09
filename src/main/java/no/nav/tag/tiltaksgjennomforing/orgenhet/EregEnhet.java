package no.nav.tag.tiltaksgjennomforing.orgenhet;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class EregEnhet {
    private String organisasjonsnummer;
    private EregNavn navn;
    private String type;

    public Organisasjon konverterTilDomeneObjekt() {
        String bedriftnavn = Stream.of(navn.getNavnelinje1(), navn.getNavnelinje2(), navn.getNavnelinje3(), navn.getNavnelinje4())
                .filter(navnelinje -> navnelinje != null)
                .collect(Collectors.joining(" "));
        return new Organisasjon(new BedriftNr(organisasjonsnummer), bedriftnavn);
    }
}
