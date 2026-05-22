package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import java.util.List;

@Value
public class EndreMål {
    List<Maal> maal;

    public boolean erTom() {
        return maal.isEmpty();
    }

    public boolean harMangler() {
        return maal.stream().anyMatch(m -> Utils.erNoenTomme(m.getBeskrivelse(), m.getKategori()));
    }
}
