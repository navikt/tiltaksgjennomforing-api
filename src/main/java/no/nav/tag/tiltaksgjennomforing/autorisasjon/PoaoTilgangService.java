package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import java.util.UUID;

public interface PoaoTilgangService {
    boolean harLeseTilgang(UUID beslutterAzureUUID, String deltakerFnr);
}
