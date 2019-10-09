package no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class TilgangskontrollAvslattProd implements TilgangskontrollAvslatt {

    @Override
    public boolean tilgangskontrollAvslatt() {
        return false;
    }

}
