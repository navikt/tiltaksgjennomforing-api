package no.nav.tag.tiltaksgjennomforing.integrasjon.ereg;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.domene.Organisasjon;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class EregEnhet {
    private String organisasjonsnummer;
    private EregNavn navn;
    private String type;

    public Organisasjon konverterTilDomeneObjekt() {
        String bedriftnavn = Stream.of(navn.getNavnelinje1(), navn.getNavnelinje2(), navn.getNavnelinje3(), navn.getNavnelinje4())
                .filter(Predicate.not(Objects::isNull))
                .collect(Collectors.joining(" "));
        return new Organisasjon(new BedriftNr(organisasjonsnummer), bedriftnavn);
    }
}
