package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;

@RequiredArgsConstructor
public class HentArbeidsgiverSelvbetjeningTokenImpl implements HentArbeidsgiverToken {
    private final TokenUtils tokenUtils;

    @Override
    public String hentArbeidsgiverToken() {
        return tokenUtils.hentSelvbetjeningToken();
    }
}
