package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import java.util.Set;
import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;

@Value
public class InnloggetVeileder implements InnloggetBruker {

    NavIdent identifikator;
    Set<String> navEnheter;
    Avtalerolle rolle = Avtalerolle.VEILEDER;
    boolean erNavAnsatt = true;
}
