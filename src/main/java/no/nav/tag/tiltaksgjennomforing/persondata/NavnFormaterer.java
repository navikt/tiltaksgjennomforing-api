package no.nav.tag.tiltaksgjennomforing.persondata;

import no.nav.team_tiltak.felles.persondata.pdl.domene.Navn;
import org.apache.commons.text.WordUtils;
import org.springframework.util.StringUtils;

public class NavnFormaterer {
    private final Navn navn;

    public NavnFormaterer(Navn navn) {
        this.navn = navn;
    }

    public String getEtternavn() {
        return storeForbokstaver(navn.etternavn());
    }

    public String getFornavn() {
        String fornavnOgMellomnavn = navn.fornavn();
        if (StringUtils.hasLength(navn.mellomnavn())) {
            fornavnOgMellomnavn += " " + navn.mellomnavn();
        }
        return storeForbokstaver(fornavnOgMellomnavn);
    }

    private static String storeForbokstaver(String navn) {
        return WordUtils.capitalizeFully(navn, '-', ' ');
    }
}
