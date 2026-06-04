package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangerDto;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;

@Value
public class InnloggetArbeidsgiver implements InnloggetBruker {
    Fnr identifikator;
    AltinnTilgangerDto altinnTilganger;
    Avtalerolle rolle = Avtalerolle.ARBEIDSGIVER;
    boolean erNavAnsatt = false;
}
