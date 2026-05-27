package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

@Value
public class EndreOppfølgingOgTilrettelegging {
    String oppfolging;
    String tilrettelegging;

    public boolean harMangler() {
        return Utils.erNoenTomme(
            oppfolging,
            tilrettelegging
        );
    }
}
