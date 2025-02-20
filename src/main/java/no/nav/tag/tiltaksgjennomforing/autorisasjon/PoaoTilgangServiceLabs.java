package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
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

    public boolean harSkrivetilgang(UUID beslutterAzureUUID, Identifikator id) {
        return true;
    }

    @Override
    public Map<Identifikator, Boolean> harSkrivetilgang(UUID beslutterAzureUUID, Set<Identifikator> idSet) {
        return idSet.stream().collect(Collectors.toMap(fnr -> fnr, fnr -> true));
    }

    @Override
    public Optional<String> hentGrunn(UUID beslutterAzureUUID, Identifikator id) {
        return Optional.empty();
    }

    @Override
    public Tilgangsattributter hentTilgangsattributter(Identifikator id) {
        return null;
    }
}
