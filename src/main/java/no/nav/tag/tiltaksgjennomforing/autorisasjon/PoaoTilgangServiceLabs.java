package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
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

    @Override
    public Map<Fnr, Boolean> harSkrivetilganger(UUID beslutterAzureUUID, Set<Fnr> fnrSet) {
        return fnrSet.stream().collect(Collectors.toMap(fnr -> fnr, fnr -> true));
    }

    @Override
    public Optional<Tilgang> hentSkrivetilgang(UUID beslutterAzureUUID, Fnr fnr) {
        return Optional.of(new Tilgang.Tillat());
    }

    @Override
    public Optional<Tilgangsattributter> hentTilgangsattributter(Fnr fnr) {
        return Optional.empty() ;
    }
}
