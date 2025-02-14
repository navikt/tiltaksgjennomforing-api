package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Profile({ Miljø.LOCAL, Miljø.TEST, Miljø.DOCKER_COMPOSE, Miljø.DEV_GCP_LABS })
public class PoaoTilgangServiceLabs implements PoaoTilgangService {

    public boolean harSkriveTilgang(UUID beslutterAzureUUID, String deltakerFnr) {
        return true;
    }
}
