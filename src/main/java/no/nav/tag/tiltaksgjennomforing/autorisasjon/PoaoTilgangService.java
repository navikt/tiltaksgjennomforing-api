package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PoaoTilgangService {
    boolean harSkrivetilgang(UUID beslutterAzureUUID, Identifikator id);
    Map<Identifikator, Boolean> harSkrivetilgang(UUID beslutterAzureUUID, Set<Identifikator> isSet);
    Optional<String> hentGrunn(UUID beslutterAzureUUID, Identifikator identifikator);
    Tilgangsattributter hentTilgangsattributter(Identifikator id);
}
