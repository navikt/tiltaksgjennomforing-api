package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PoaoTilgangService {
    Optional<Tilgang> hentSkrivetilgang(UUID beslutterAzureUUID, Fnr fnr);
    Map<Fnr, Boolean> harSkrivetilganger(UUID beslutterAzureUUID, Set<Fnr> fnrSet);
    Optional<Tilgangsattributter> hentTilgangsattributter(Fnr fnr);
}
