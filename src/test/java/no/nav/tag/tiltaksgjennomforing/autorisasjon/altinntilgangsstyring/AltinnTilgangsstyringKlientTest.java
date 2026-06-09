package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.IntegrasjonerMockServer;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.exceptions.AltinnFeilException;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.CacheConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles({Miljø.TEST, Miljø.WIREMOCK})
@DirtiesContext
class AltinnTilgangsstyringKlientTest {

    private static final String ALTINN_TILGANGER_PATH = "/altinn-tilganger";

    private static final String SUCCESS_BODY_TEMPLATE = """
        {
          "isError": false,
          "hierarki": [],
          "orgNrTilTilganger": {"%s": []},
          "tilgangTilOrgNr": {}
        }
        """;

    @Autowired
    private AltinnTilgangsstyringKlient klient;

    @Autowired
    private IntegrasjonerMockServer integrasjonerMockServer;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
        integrasjonerMockServer.getServer().resetAll();
        Objects.requireNonNull(cacheManager.getCache(CacheConfig.ALTINN_CACHE)).clear();
    }

    @AfterEach
    void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    @Test
    void cache_returnerer_ulikt_resultat_for_ulike_fnr() {
        integrasjonerMockServer.getServer()
            .stubFor(WireMock.post(urlPathEqualTo(ALTINN_TILGANGER_PATH))
                .atPriority(0)
                .inScenario("cache-fnr-test")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(okJson(SUCCESS_BODY_TEMPLATE.formatted("111111111")))
                .willSetStateTo("andre-kall"));

        integrasjonerMockServer.getServer()
            .stubFor(WireMock.post(urlPathEqualTo(ALTINN_TILGANGER_PATH))
                .atPriority(0)
                .inScenario("cache-fnr-test")
                .whenScenarioStateIs("andre-kall")
                .willReturn(okJson(SUCCESS_BODY_TEMPLATE.formatted("222222222"))));

        Fnr fnr1 = Fnr.generer(25);
        Fnr fnr2 = Fnr.generer(30);

        AltinnTilgangerResponse response1 = klient.kallAltinn3(fnr1);
        AltinnTilgangerResponse response2 = klient.kallAltinn3(fnr2);

        assertThat(response1.orgNrTilTilganger()).containsKey("111111111");
        assertThat(response2.orgNrTilTilganger()).containsKey("222222222");
        assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    void cache_kaller_ikke_altinn_igjen_for_samme_fnr() {
        integrasjonerMockServer.getServer()
            .stubFor(WireMock.post(urlPathEqualTo(ALTINN_TILGANGER_PATH))
                .atPriority(0)
                .willReturn(okJson(SUCCESS_BODY_TEMPLATE.formatted("999999999"))));

        Fnr fnr = Fnr.generer(25);

        klient.kallAltinn3(fnr);
        klient.kallAltinn3(fnr);

        integrasjonerMockServer.getServer().verify(1, postRequestedFor(urlPathEqualTo(ALTINN_TILGANGER_PATH)));
    }

    @Test
    void retry_forsøker_på_nytt_ved_502() {
        integrasjonerMockServer.getServer()
            .stubFor(WireMock.post(urlPathEqualTo(ALTINN_TILGANGER_PATH))
                .atPriority(0)
                .inScenario("retry-502")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(502))
                .willSetStateTo("retry-1"));

        integrasjonerMockServer.getServer()
            .stubFor(WireMock.post(urlPathEqualTo(ALTINN_TILGANGER_PATH))
                .atPriority(0)
                .inScenario("retry-502")
                .whenScenarioStateIs("retry-1")
                .willReturn(okJson(SUCCESS_BODY_TEMPLATE.formatted("999999999"))));

        AltinnTilgangerResponse response = klient.kallAltinn3(Fnr.generer(25));

        assertThat(response).isNotNull();
        assertThat(response.orgNrTilTilganger()).containsKey("999999999");
        integrasjonerMockServer.getServer().verify(2, postRequestedFor(urlPathEqualTo(ALTINN_TILGANGER_PATH)));
    }

    @Test
    void retry_forsøker_på_nytt_ved_503() {
        integrasjonerMockServer.getServer()
            .stubFor(WireMock.post(urlPathEqualTo(ALTINN_TILGANGER_PATH))
                .atPriority(0)
                .inScenario("retry-503")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("retry-1"));

        integrasjonerMockServer.getServer()
            .stubFor(WireMock.post(urlPathEqualTo(ALTINN_TILGANGER_PATH))
                .atPriority(0)
                .inScenario("retry-503")
                .whenScenarioStateIs("retry-1")
                .willReturn(okJson(SUCCESS_BODY_TEMPLATE.formatted("999999999"))));

        AltinnTilgangerResponse response = klient.kallAltinn3(Fnr.generer(25));

        assertThat(response).isNotNull();
        assertThat(response.orgNrTilTilganger()).containsKey("999999999");
        integrasjonerMockServer.getServer().verify(2, postRequestedFor(urlPathEqualTo(ALTINN_TILGANGER_PATH)));
    }

    @Test
    void retry_kaster_exception_etter_maks_forsøk() {
        integrasjonerMockServer.getServer()
            .stubFor(WireMock.post(urlPathEqualTo(ALTINN_TILGANGER_PATH))
                .atPriority(0)
                .willReturn(aResponse().withStatus(502)));

        assertThatThrownBy(() -> klient.kallAltinn3(Fnr.generer(25))).isInstanceOf(AltinnFeilException.class);

        // Spring Retry default maxAttempts = 3
        integrasjonerMockServer.getServer().verify(3, postRequestedFor(urlPathEqualTo(ALTINN_TILGANGER_PATH)));
    }
}
