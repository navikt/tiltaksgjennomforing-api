package no.nav.tag.tiltaksgjennomforing.domene.autorisasjon;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.Identifikator;

@Data
public abstract class InnloggetBruker<T extends Identifikator> {
    private final T identifikator;
}
