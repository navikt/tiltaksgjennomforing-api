package no.nav.tag.tiltaksgjennomforing.digitalkommunikasjon;

import no.nav.tag.tiltaksgjennomforing.IntegrasjonerMockServer;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({ Miljø.TEST, Miljø.WIREMOCK })
@DirtiesContext
class KrrClientIntegrasjonTest {

    private static final String KRR_PERSON_ENDEPUNKT = "/digdir-krr-proxy/rest/v1/personer";

    @Autowired
    private KrrClient krrClient;

    @Autowired
    private IntegrasjonerMockServer integrasjonerMockServer;

    @BeforeEach
    void setUp() {
        integrasjonerMockServer.getServer().resetRequests();
    }

    @Test
    void hentPersonReservertForDigitalKontakt_returnererTrueFraKrrRespons() {
        Fnr fnr = Fnr.fraDb("12345678910");

        assertThat(krrClient.hentPersonReservertForDigitalKontakt(fnr)).contains(true);

        integrasjonerMockServer.getServer().verify(postRequestedFor(urlPathEqualTo(KRR_PERSON_ENDEPUNKT))
            .withHeader("Accept", containing("application/json"))
            .withHeader("Content-Type", containing("application/json"))
            .withRequestBody(equalToJson("""
                {
                  "personidenter": ["12345678910"]
                }
                """)));
    }

    @Test
    void hentPersonReservertForDigitalKontakt_returnererFalseFraKrrRespons() {
        Fnr fnr = Fnr.fraDb("11111111111");

        assertThat(krrClient.hentPersonReservertForDigitalKontakt(fnr)).contains(false);

        integrasjonerMockServer.getServer().verify(postRequestedFor(urlPathEqualTo(KRR_PERSON_ENDEPUNKT))
            .withRequestBody(equalToJson("""
                {
                  "personidenter": ["11111111111"]
                }
                """)));
    }
}

