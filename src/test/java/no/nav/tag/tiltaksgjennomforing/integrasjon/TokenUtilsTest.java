package no.nav.tag.tiltaksgjennomforing.integrasjon;

import no.nav.security.oidc.context.OIDCClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.oidc.context.OIDCValidationContext;
import no.nav.security.oidc.context.TokenContext;
import no.nav.tag.tiltaksgjennomforing.domene.TestData;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetSelvbetjeningBruker;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static no.nav.security.oidc.test.support.JwtTokenGenerator.createSignedJWT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TokenUtilsTest {

    @InjectMocks
    private TokenUtils tilgangskontroll;

    @Mock
    private OIDCRequestContextHolder contextHolder;

    @Test
    public void hentInnloggetBruker__er_selvbetjeningbruker() {
        InnloggetSelvbetjeningBruker selvbetjeningBruker = TestData.enSelvbetjeningBruker();
        vaerInnloggetSelvbetjening(selvbetjeningBruker);
        assertThat(tilgangskontroll.hentInnloggetBruker()).isEqualTo(selvbetjeningBruker);
    }

    @Test
    public void hentInnloggetBruker__er_nav_ansatt() {
        InnloggetNavAnsatt navAnsatt = TestData.enNavAnsatt();
        vaerInnloggetNavAnsatt(navAnsatt);
        assertThat(tilgangskontroll.hentInnloggetBruker()).isEqualTo(navAnsatt);
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentInnloggetSelvbetjeningBruker__er_nav_ansatt() {
        vaerInnloggetNavAnsatt(TestData.enNavAnsatt());
        tilgangskontroll.hentInnloggetSelvbetjeningBruker();
    }


    @Test(expected = TilgangskontrollException.class)
    public void hentInnloggetNavAnsatt__er_selvbetjeningbruker() {
        vaerInnloggetSelvbetjening(TestData.enSelvbetjeningBruker());
        tilgangskontroll.hentInnloggetNavAnsatt();
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentInnloggetBruker__er_uinnlogget() {
        vaerUinnlogget();
        tilgangskontroll.hentInnloggetBruker();
    }

    private void vaerUinnlogget() {
        OIDCValidationContext context = new OIDCValidationContext();
        when(contextHolder.getOIDCValidationContext()).thenReturn(context);
    }

    private void vaerInnloggetSelvbetjening(InnloggetSelvbetjeningBruker bruker) {
        vaerInnloggetNavAnsatt(TokenUtils.ISSUER_SELVBETJENING, bruker.getIdentifikator().asString(), new HashMap<>(), "aud-selvbetjening");
    }

    private void vaerInnloggetNavAnsatt(InnloggetNavAnsatt innloggetBruker) {
        vaerInnloggetNavAnsatt(TokenUtils.ISSUER_ISSO, "blablabla", Collections.singletonMap("NAVident", innloggetBruker.getIdentifikator().asString()), "aud-isso");
    }

    private void vaerInnloggetNavAnsatt(String issuer, String subject, Map<String, Object> claims, String audience) {
        OIDCValidationContext context = new OIDCValidationContext();
        TokenContext tokenContext = new TokenContext(issuer, "");
        OIDCClaims oidcClaims = new OIDCClaims(createSignedJWT(subject, 0, claims, issuer, audience));
        context.addValidatedToken(issuer, tokenContext, oidcClaims);

        when(contextHolder.getOIDCValidationContext()).thenReturn(context);
    }
}