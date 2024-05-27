package no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbtilgang;

import java.util.UUID;

interface PoaoTilgangService {
    boolean harSkriveTilgang(UUID beslutterAzureUUID, String deltakerFnr);
}
