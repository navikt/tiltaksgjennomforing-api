package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = { Miljø.LABS_GCP })
public class ArbeidsgiverTokenStrategyFactoryLabsMock implements ArbeidsgiverTokenStrategyFactory {

    @Override
    public HentArbeidsgiverToken create(TokenUtils.Issuer issuer) {
        return () -> "";
    }
}