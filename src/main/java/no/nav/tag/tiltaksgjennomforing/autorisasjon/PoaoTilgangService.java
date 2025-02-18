package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PoaoTilgangService {
    boolean harSkrivetilgang(UUID beslutterAzureUUID, Fnr fnr);
    Map<Fnr, Boolean> harSkrivetilgang(UUID beslutterAzureUUID, Set<Fnr> fnrListe);
    Optional<String> hentGrunn(UUID beslutterAzureUUID, Fnr fnr);
}
