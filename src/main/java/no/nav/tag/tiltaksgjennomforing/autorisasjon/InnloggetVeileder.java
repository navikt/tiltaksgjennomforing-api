package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;

import java.util.Set;

@Value
public class InnloggetVeileder implements InnloggetBruker {
    NavIdent identifikator;
    Set<NavEnhet> navEnheter;
    Avtalerolle rolle = Avtalerolle.VEILEDER;
    boolean erNavAnsatt = true;
    boolean kanVæreBeslutter;
}
