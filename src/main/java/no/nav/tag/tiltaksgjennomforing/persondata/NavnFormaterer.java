package no.nav.tag.tiltaksgjennomforing.persondata;

import org.apache.commons.text.WordUtils;
import org.springframework.util.StringUtils;

public class NavnFormaterer {
    private final Navn navn;

    public NavnFormaterer(Navn navn) {
        this.navn = navn;
    }

    public String getEtternavn() {
        return storeBokstaver(navn.getEtternavn());
    }


    public String getFornavn() {
        var fornavnOgMellomnavn = navn.getFornavn();
        if (StringUtils.hasLength(navn.getMellomnavn())) {
            fornavnOgMellomnavn += " " + navn.getMellomnavn();
        }
        return storeBokstaver(fornavnOgMellomnavn);
    }

    private static String storeBokstaver(String navn) {
        return WordUtils.capitalizeFully(navn, '-', ' ');
    }
}
