package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.SneakyThrows;
import no.nav.security.oidc.context.TokenContext;
import no.nav.security.oidc.test.support.JwtTokenGenerator;
import no.nav.tag.tiltaksgjennomforing.AltinnProxyOgAltinnMockServer;
import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnOrganisasjon;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringService;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.ArbeidsgiverOrganisasjon;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils.Issuer.ISSUER_SELVBETJENING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"dev", "wiremock"})
@DirtiesContext
public class AltinnTilgangsstyringServiceMedProxyTest {
    @Autowired
    private AltinnTilgangsstyringService altinnTilgangsstyringService;

    @Autowired
    private AltinnProxyOgAltinnMockServer altinnProxyOgAltinnMockServer;

    @MockBean
    private TokenUtils tokenUtils;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Before
    public void setUp() {
        when(featureToggleService.isEnabled("arbeidsgiver.tiltaksgjennomforing-api.bruk-altinn-proxy"))
                .thenReturn(true);
    }

    @Test
    public void hentOrganisasjoner__gyldig_fnr_en_bedrift_pa_hvert_titlak() {
        Fnr innloggetBruker = new Fnr("10000000000");
        String userToken = opprettOgSetSelvbetjeningTokenContext(innloggetBruker);
        altinnProxyOgAltinnMockServer.stubForAltinnProxy(
                userToken,
                "5516",
                "1",
                getAltinnOrganisasjon("Bedriften Med Midlertidig Lts", "910712314")
        );
        altinnProxyOgAltinnMockServer.stubForAltinnProxy(
                userToken,
                "5516",
                "2",
                getAltinnOrganisasjon("Bedriften Med Varig Lts", "910712306")
        );
        altinnProxyOgAltinnMockServer.stubForAltinnProxy(
                userToken,
                "5332",
                "1",
                getAltinnOrganisasjon("Saltrød og Høneby", "999999999")
        );

        List<ArbeidsgiverOrganisasjon> organisasjoner =
                altinnTilgangsstyringService.hentOrganisasjoner(innloggetBruker);

        assertThat(organisasjoner)
                .extracting("bedriftNr")
                .containsOnly(
                        new BedriftNr("999999999"),
                        new BedriftNr("910712314"),
                        new BedriftNr("910712306")
                );
        assertThat(organisasjoner)
                .flatExtracting(ArbeidsgiverOrganisasjon.Fields.tilgangstyper)
                .contains(
                        Tiltakstype.ARBEIDSTRENING,
                        Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
                        Tiltakstype.VARIG_LONNSTILSKUDD
                );
    }

    @Test
    public void hentOrganisasjoner__fnr_skal_ha_tilgang_pa_arbeidstrening() {
        Fnr innloggetBruker = new Fnr("20000000000");
        String userToken = opprettOgSetSelvbetjeningTokenContext(innloggetBruker);
        altinnProxyOgAltinnMockServer.stubForAltinnProxy(
                userToken,
                "5332",
                "1",
                getAltinnOrganisasjon("SOPRA STERIA AS", "910909088"),
                getAltinnOrganisasjon("KIWI LØREN AS", "981121465")
        );
        altinnProxyOgAltinnMockServer.stubForAltinnProxy(userToken, "5516", "1");
        altinnProxyOgAltinnMockServer.stubForAltinnProxy(userToken, "5516", "2");

        List<ArbeidsgiverOrganisasjon> organisasjoner =
                altinnTilgangsstyringService.hentOrganisasjoner(innloggetBruker);

        assertThat(organisasjoner)
                .flatExtracting(ArbeidsgiverOrganisasjon.Fields.tilgangstyper)
                .containsOnly(Tiltakstype.ARBEIDSTRENING);
        assertThat(organisasjoner)
                .extracting("bedriftNr")
                .containsOnly(
                        new BedriftNr("981121465"),
                        new BedriftNr("910909088")
                );
    }

    @Test
    public void hentOrganisasjoner__fnr_skal_ha_tilgang_pa_midlertidig_lonnstilskudd() {
        Fnr innloggetBruker = new Fnr("26000000000");
        String userToken = opprettOgSetSelvbetjeningTokenContext(innloggetBruker);
        altinnProxyOgAltinnMockServer.stubForAltinnProxy(userToken, "5332", "1");
        altinnProxyOgAltinnMockServer.stubForAltinnProxy(
                userToken,
                "5516",
                "1",
                getAltinnOrganisasjon("HUNNDALEN OG NEVERDAL", "910712306")
        );
        altinnProxyOgAltinnMockServer.stubForAltinnProxy(userToken, "5516", "2");

        List<ArbeidsgiverOrganisasjon> organisasjoner =
                altinnTilgangsstyringService.hentOrganisasjoner(innloggetBruker);

        assertThat(organisasjoner)
                .flatExtracting(ArbeidsgiverOrganisasjon.Fields.tilgangstyper)
                .containsOnly(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
    }

    @Test
    public void hentOrganisasjoner__fnr_skal_ha_tilgang_pa_varig_lonnstilskudd() {
        Fnr innloggetBruker = new Fnr("27000000000");
        String userToken = opprettOgSetSelvbetjeningTokenContext(innloggetBruker);
        altinnProxyOgAltinnMockServer.stubForAltinnProxy(userToken, "5332", "1");
        altinnProxyOgAltinnMockServer.stubForAltinnProxy(userToken, "5516", "1");
        altinnProxyOgAltinnMockServer.stubForAltinnProxy(
                userToken,
                "5516",
                "2",
                getAltinnOrganisasjon("BIRTAVARRE OG VÆRLANDET REGNSKAP", "910825607")
        );

        List<ArbeidsgiverOrganisasjon> organisasjoner =
                altinnTilgangsstyringService.hentOrganisasjoner(innloggetBruker);
        assertThat(organisasjoner)
                .flatExtracting(ArbeidsgiverOrganisasjon.Fields.tilgangstyper)
                .containsOnly(Tiltakstype.VARIG_LONNSTILSKUDD);
    }

    @Test
    public void hentOrganisasjoner__gyldig_fnr_tom_liste() {
        Fnr innloggetBruker = new Fnr("00000000000");
        String userToken = opprettOgSetSelvbetjeningTokenContext(innloggetBruker);
        altinnProxyOgAltinnMockServer.stubForAltinnProxy(userToken, "5332", "1");
        altinnProxyOgAltinnMockServer.stubForAltinnProxy(userToken, "5516", "1");
        altinnProxyOgAltinnMockServer.stubForAltinnProxy(userToken, "5516", "2");

        List<ArbeidsgiverOrganisasjon> organisasjoner = altinnTilgangsstyringService.
                hentOrganisasjoner(innloggetBruker);
        assertThat(organisasjoner).isEmpty();
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void hentOrganisasjoner__ugyldig_fnr_skal_kaste_feil() {
        Fnr innloggetBruker = new Fnr("00000000000");
        String userToken = opprettOgSetSelvbetjeningTokenContext(innloggetBruker);
        altinnProxyOgAltinnMockServer.stubForAltinnProxyBadRequest(userToken, "5332", "1");
        altinnProxyOgAltinnMockServer.stubForAltinnBadRequest(innloggetBruker, "5332", "1");

        altinnTilgangsstyringService.hentOrganisasjoner(innloggetBruker);
    }

    @SneakyThrows
    @Test(expected = TiltaksgjennomforingException.class)
    public void hentOrganisasjoner__feilkonfigurasjon_skal_kaste_feil() {
        AltinnTilgangsstyringProperties altinnTilgangsstyringProperties = new AltinnTilgangsstyringProperties();
        altinnTilgangsstyringProperties.setProxyUrl(new URL("http://foobar"));
        altinnTilgangsstyringProperties.setProxyFallbackUrl(new URL("http://foobarbazz"));
        altinnTilgangsstyringProperties.setAltinnApiKey("test");
        altinnTilgangsstyringProperties.setAltinnHeader("test");
        altinnTilgangsstyringProperties.setAPIGwHeader("test");
        altinnTilgangsstyringProperties.setApiGwApiKey("test");
        altinnTilgangsstyringProperties.setLtsMidlertidigServiceEdition(1111);
        altinnTilgangsstyringProperties.setLtsMidlertidigServiceCode(1);
        altinnTilgangsstyringProperties.setLtsVarigServiceEdition(3333);
        altinnTilgangsstyringProperties.setLtsVarigServiceCode(3);
        altinnTilgangsstyringProperties.setArbtreningServiceCode(2222);
        altinnTilgangsstyringProperties.setArbtreningServiceEdition(2);

        new AltinnTilgangsstyringService(
                altinnTilgangsstyringProperties,
                tokenUtils,
                featureToggleService
        ).hentOrganisasjoner(
                TestData.enIdentifikator()
        );
    }

    @Test
    public void hentOrganisasjoner__fallback_kall_til_Altinn_dersom_Proxy_er_unavailable() {
        Fnr innloggetBruker = new Fnr("20000000000");
        String userToken = opprettOgSetSelvbetjeningTokenContext(innloggetBruker);
        altinnProxyOgAltinnMockServer.stubForAltinnProxy(
                userToken,
                "5332",
                "1",
                getAltinnOrganisasjon("SOPRA STERIA AS", "910909088"),
                getAltinnOrganisasjon("KIWI LØREN AS", "981121465")
        );
        altinnProxyOgAltinnMockServer.stubForAltinnProxyUnavailable(userToken, "5332", "1");
        altinnProxyOgAltinnMockServer.stubForAltinnProxyUnavailable(userToken, "5516", "1");
        altinnProxyOgAltinnMockServer.stubForAltinnProxyUnavailable(userToken, "5516", "2");
        altinnProxyOgAltinnMockServer.stubForAltinn(
                innloggetBruker,
                "5332",
                "1",
                getAltinnOrganisasjon("KIWI LØREN AS", "981121465")
        );
        altinnProxyOgAltinnMockServer.stubForAltinn(innloggetBruker, "5516", "1");
        altinnProxyOgAltinnMockServer.stubForAltinn(innloggetBruker, "5516", "2");

        List<ArbeidsgiverOrganisasjon> organisasjoner = altinnTilgangsstyringService.hentOrganisasjoner(innloggetBruker);

        assertThat(organisasjoner)
                .flatExtracting(ArbeidsgiverOrganisasjon.Fields.tilgangstyper)
                .containsOnly(Tiltakstype.ARBEIDSTRENING);
        assertThat(organisasjoner)
                .extracting("bedriftNr")
                .containsOnly(
                        new BedriftNr("981121465")
                );
    }

    private String opprettOgSetSelvbetjeningTokenContext(Fnr fnr) {
        String token = getUserToken(fnr);
        setTokenContext(token);
        return token;
    }

    private void setTokenContext(String token) {
        when(tokenUtils.hentSelvbetjeningTokenContext()).thenReturn(
                new TokenContext(
                        ISSUER_SELVBETJENING.issuerName,
                        token
                )
        );
    }

    private static AltinnOrganisasjon getAltinnOrganisasjon(String name, String organizationNumber) {
        AltinnOrganisasjon organisasjon = new AltinnOrganisasjon();
        organisasjon.setName(name);
        organisasjon.setType("Business");
        organisasjon.setOrganizationNumber(organizationNumber);
        organisasjon.setOrganizationForm("BEDR");
        organisasjon.setStatus("Active");
        return organisasjon;
    }

    private static String getUserToken(Fnr subject) {
        JWTClaimsSet jwtClaimsSet = JwtTokenGenerator.buildClaimSet(
                subject.asString(),
                "iss-localhost",
                "aud-localhost",
                "Level4",
                TimeUnit.MINUTES.toMillis(15),
                new HashMap<>());

        return JwtTokenGenerator.createSignedJWT(jwtClaimsSet).serialize();
    }
}
