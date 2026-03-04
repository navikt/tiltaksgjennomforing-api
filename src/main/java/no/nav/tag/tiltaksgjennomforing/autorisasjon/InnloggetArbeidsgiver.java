package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import lombok.Value;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangerResponse;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Value
public class InnloggetArbeidsgiver implements InnloggetBruker {
    Fnr identifikator;
    Set<AltinnReportee> altinnOrganisasjoner;
    Map<BedriftNr, Collection<Tiltakstype>> tilganger;
    AltinnTilgangerResponse altinn3Tilganger;
    Avtalerolle rolle = Avtalerolle.ARBEIDSGIVER;
    boolean erNavAnsatt = false;
}
