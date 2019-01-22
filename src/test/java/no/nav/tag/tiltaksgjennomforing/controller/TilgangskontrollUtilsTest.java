package no.nav.tag.tiltaksgjennomforing.controller;

import no.nav.security.oidc.context.OIDCClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.oidc.context.OIDCValidationContext;
import no.nav.security.oidc.context.TokenContext;
import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.domene.*;
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
public class TilgangskontrollUtilsTest {

    @InjectMocks
    private TilgangskontrollUtils tilgangskontroll;

    @Mock
    private OIDCRequestContextHolder contextHolder;

    @Test
    public void skalHenteInnloggetBrukerFraToken() {
        Bruker bruker = TestData.deltaker();
        vaerInnloggetSom(bruker);
        assertThat(tilgangskontroll.hentInnloggetPerson()).isEqualTo(bruker);
    }

    @Test
    public void skalHenteInnloggetVeilederFraToken() {
        Veileder veileder = TestData.veileder();
        vaerInnloggetSom(veileder);
        assertThat(tilgangskontroll.hentInnloggetPerson()).isEqualTo(veileder);
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentInnloggetVeilederSkalFeileHvisInnloggetBrukerIkkeErVeileder() {
        vaerInnloggetSom(TestData.deltaker());
        tilgangskontroll.hentInnloggetVeileder();
    }


    private void vaerInnloggetSom(Bruker bruker) {
        vaerInnloggetSom(TilgangskontrollUtils.ISSUER_SELVBETJENING, bruker.getFnr().getFnr(), new HashMap<>(), "aud-selvbetjening");
    }

    private void vaerInnloggetSom(Veileder veileder) {
        vaerInnloggetSom(TilgangskontrollUtils.ISSUER_ISSO, "blablabla", Collections.singletonMap("NAVident", veileder.getNavIdent().getId()), "aud-isso");
    }

    private void vaerInnloggetSom(String issuer, String subject, Map<String, Object> claims, String audience) {
        OIDCValidationContext context = new OIDCValidationContext();
        TokenContext tokenContext = new TokenContext(issuer, "");
        OIDCClaims oidcClaims = new OIDCClaims(createSignedJWT(subject, 0, claims, issuer, audience));
        context.addValidatedToken(issuer, tokenContext, oidcClaims);

        when(contextHolder.getOIDCValidationContext()).thenReturn(context);
    }

    @Test(expected = TilgangskontrollException.class)
    public void hentInnloggetPersonSkalKasteTilgangskontrollExceptionHvisIkkeInnlogget() {
        vaerUinnlogget();
        tilgangskontroll.hentInnloggetPerson();
    }

    private void vaerUinnlogget() {
        OIDCValidationContext context = new OIDCValidationContext();
        when(contextHolder.getOIDCValidationContext()).thenReturn(context);
    }
}