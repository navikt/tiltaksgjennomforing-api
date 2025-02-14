package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import java.util.Optional;
import java.util.UUID;

public interface PoaoTilgangService {
    boolean harSkrivetilgang(UUID beslutterAzureUUID, String deltakerFnr);
    Optional<String> hentGrunn(UUID beslutterAzureUUID, String deltakerFnr);
}
