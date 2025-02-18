package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.persondata.aktorId.AktorId;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PoaoTilgangService {
    boolean harSkrivetilgang(UUID beslutterAzureUUID, AktorId aktorId);
    Map<AktorId, Boolean> harSkrivetilgang(UUID beslutterAzureUUID, Set<AktorId> aktorIdSet);
    Optional<String> hentGrunn(UUID beslutterAzureUUID, AktorId aktorId);
}
