package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.HentArbeidsgiverToken;

public interface ArbeidsgiverTokenStrategyFactoryInterface {
    HentArbeidsgiverToken create(TokenUtils.Issuer issuer);
}
