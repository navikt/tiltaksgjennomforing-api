package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record AltinnTilgangerDto(
    List<AltinnTilgang> hierarki,
    Map<BedriftNr, Set<Tiltakstype>> tilganger,
    List<BedriftNr> adressesperreTilganger
) {}
