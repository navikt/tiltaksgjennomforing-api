package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.persondata.aktorId.AktorId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile({ Miljø.LOCAL, Miljø.TEST, Miljø.DOCKER_COMPOSE, Miljø.DEV_GCP_LABS })
public class PoaoTilgangServiceLabs implements PoaoTilgangService {

    public boolean harSkrivetilgang(UUID beslutterAzureUUID, AktorId aktorId) {
        return true;
    }

    @Override
    public Map<AktorId, Boolean> harSkrivetilgang(UUID beslutterAzureUUID, Set<AktorId> aktorIdSet) {
        return aktorIdSet.stream().collect(Collectors.toMap(aktorId -> aktorId, aktorId -> true));
    }

    @Override
    public Optional<String> hentGrunn(UUID beslutterAzureUUID, AktorId aktorId) {
        return Optional.empty();
    }
}
