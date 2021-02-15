package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import static no.nav.security.jwt.test.support.JwtTokenGenerator.ACR_LEVEL_4;
import static no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.Issuer.ISSUER_ISSO;
import static no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.Issuer.ISSUER_SELVBETJENING;
import static no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.Issuer.ISSUER_SYSTEM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import no.nav.security.jwt.test.support.JwkGenerator;
import no.nav.security.jwt.test.support.JwtTokenGenerator;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.BrukerOgIssuer;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.Issuer;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TokenUtilsTest {

    @InjectMocks
    private TokenUtils tokenUtils;

    @Mock
    private TokenValidationContextHolder contextHolder;

    @Test
    public void hentInnloggetBruker__er_selvbetjeningbruker() {
        InnloggetArbeidsgiver selvbetjeningBruker = TestData.enInnloggetArbeidsgiver();
        vaerInnloggetSelvbetjening(selvbetjeningBruker);
        assertThat(tokenUtils.hentBrukerOgIssuer().get())
            .isEqualTo(new BrukerOgIssuer(ISSUER_SELVBETJENING, selvbetjeningBruker.getIdentifikator().asString()));
    }

    @Test
    public void hentBeslutter_fra_nav_ansatt_innloggetBruker_med_riktig_beslutter_gruppe() {
        InnloggetBeslutter navBeslutter = TestData.enInnloggetBeslutter();
        vaerInnloggetNavAnsatt(navBeslutter);
        assertThat(tokenUtils.hentBrukerOgIssuer().get()).isEqualTo(new BrukerOgIssuer(ISSUER_ISSO, navBeslutter.getIdentifikator().asString()));
        UUID beslutterAdGruppe = UUID.fromString("928636f4-fd0d-4149-978e-a6fb68bb19de");
        assertThat(tokenUtils.harAdGruppe(beslutterAdGruppe)).isTrue();
    }

    @Test
    public void hentBeslutter_fra_nav_ansatt_innloggetBruker_returnerer_false_naar_ad_gruppe_ikke_finnes_for_palogget_bruker() {
        InnloggetBeslutter navBeslutter = TestData.enInnloggetBeslutter();
        vaerInnloggetNavAnsatt(navBeslutter);
        assertThat(tokenUtils.hentBrukerOgIssuer().get()).isEqualTo(new BrukerOgIssuer(ISSUER_ISSO, navBeslutter.getIdentifikator().asString()));
        assertThat(tokenUtils.harAdGruppe(UUID.randomUUID())).isFalse();
    }

    @Test
    public void hentInnloggetBruker__er_selvbetjeningbruker_må_være_nivå_4() {
        InnloggetArbeidsgiver selvbetjeningBruker = TestData.enInnloggetArbeidsgiver();
        vaerInnloggetSelvbetjening(selvbetjeningBruker);
        assertThat(tokenUtils.hentBrukerOgIssuer().get())
            .isEqualTo(new BrukerOgIssuer(ISSUER_SELVBETJENING, selvbetjeningBruker.getIdentifikator().asString()));
        vaerInnloggetSelvbetjeningNiva3(selvbetjeningBruker);
        assertThat(tokenUtils.hentBrukerOgIssuer().isEmpty()).isTrue();
    }
    
    @Test
    public void hentInnloggetBruker__er_nav_ansatt() {
        InnloggetVeileder navAnsatt = TestData.enInnloggetVeileder();
        vaerInnloggetNavAnsatt(navAnsatt);
        assertThat(tokenUtils.hentBrukerOgIssuer().get()).isEqualTo(new BrukerOgIssuer(ISSUER_ISSO, navAnsatt.getIdentifikator().asString()));
    }

    @Test
    public void hentInnloggetBruker__er_system() {
        vaerInnloggetSystem("systemId");
        assertThat(tokenUtils.hentBrukerOgIssuer().get()).isEqualTo(new BrukerOgIssuer(Issuer.ISSUER_SYSTEM, "systemId"));
    }

    @Test
    public void hentInnloggetBruker__er_uinnlogget() {
        vaerUinnlogget();
        assertThat(tokenUtils.hentBrukerOgIssuer().isEmpty()).isTrue();
    }

    private void vaerUinnlogget() {
        JWTClaimsSet claimsSet = new Builder().build();
        Map<String, JwtToken> tokenMap = new HashMap<>();
        String tokenAsString = JwtTokenGenerator.createSignedJWT(JwkGenerator.getDefaultRSAKey(), claimsSet).serialize();
        JwtToken token = new JwtToken(tokenAsString);
        tokenMap.put(token.getIssuer(), token);
        TokenValidationContext context = new TokenValidationContext(tokenMap);
        when(contextHolder.getTokenValidationContext()).thenReturn(context);
    }

    private void vaerInnloggetSystem(String systemId) {
        lagTokenValidationContext(ISSUER_SYSTEM, systemId, null, null, null);
    }

    private void vaerInnloggetSelvbetjening(InnloggetArbeidsgiver bruker) {
        lagTokenValidationContext(ISSUER_SELVBETJENING, bruker.getIdentifikator().asString(), null, ACR_LEVEL_4, null);
    }

    private void vaerInnloggetSelvbetjeningNiva3(InnloggetArbeidsgiver bruker) {
        lagTokenValidationContext(ISSUER_SELVBETJENING, bruker.getIdentifikator().asString(), null, "Level3", null);
    }

    private void vaerInnloggetNavAnsatt(InnloggetVeileder innloggetBruker) {
        lagTokenValidationContext(ISSUER_ISSO, "blablabla",  innloggetBruker.getIdentifikator().asString(), null, null);
    }

    private void vaerInnloggetNavAnsatt(InnloggetBeslutter innloggetBruker) {
        lagTokenValidationContext(ISSUER_ISSO, "blablabla",  innloggetBruker.getIdentifikator().asString(), null,
            Arrays.asList("928636f4-fd0d-4149-978e-a6fb68bb19de", "158234a2-fd1d-4445-578e-a6fb68bb11das"));
    }

    private void lagTokenValidationContext(Issuer issuer, String subject, String navIdent, String acrLevel, List groups) {
        Date now = new Date();
        JWTClaimsSet claimsSet = new Builder()
            .subject(subject)
            .claim("NAVident", navIdent)
            .issuer(issuer.issuerName)
            .audience("aud-aad")
            .jwtID(UUID.randomUUID().toString())
            .claim("groups", groups)
            .claim("acr",acrLevel)
            .claim("ver", "1.0")
            .claim("auth_time", now)
            .claim("nonce", "myNonce")
            .notBeforeTime(now)
            .issueTime(now)
            .expirationTime(new Date(now.getTime() + 1000000)).build();

        String tokenAsString = JwtTokenGenerator.createSignedJWT(JwkGenerator.getDefaultRSAKey(), claimsSet).serialize();
        Map<String, JwtToken> tokenMap = new HashMap<>();
        JwtToken token = new JwtToken(tokenAsString);
        tokenMap.put(token.getIssuer(), token);
        TokenValidationContext context = new TokenValidationContext(tokenMap);

        when(contextHolder.getTokenValidationContext()).thenReturn(context);
    }
}