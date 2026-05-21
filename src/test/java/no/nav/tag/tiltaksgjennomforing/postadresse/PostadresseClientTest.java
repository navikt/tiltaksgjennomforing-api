package no.nav.tag.tiltaksgjennomforing.postadresse;

import no.nav.tag.tiltaksgjennomforing.IntegrasjonerMockServer;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.postadresse.exception.RegoppslagFunctionalException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles({ Miljø.TEST, Miljø.WIREMOCK })
@DirtiesContext
class PostadresseClientTest {

    @Autowired
    private PostadresseClient postadresseClient;

    @Autowired
    private IntegrasjonerMockServer integrasjonerMockServer;

    @Test
    void hentPostadresse__skal_returnere_hele_responsen_fra_regoppslag() {
        PostadresseResponse response = postadresseClient.hentPostadresse(Fnr.fraDb("09876543210"));

        assertThat(response.navn()).isEqualTo("Jan Neimansen");
        assertThat(response.adresse()).isNotNull();
        assertThat(response.adresse().adresselinje1()).isEqualTo("eksempelveien 23 A");

        integrasjonerMockServer.getServer().verify(
            postRequestedFor(urlPathEqualTo("/regoppslag/rest/postadresse"))
                .withRequestBody(matchingJsonPath("$.ident", equalTo("09876543210")))
        );
    }

    @Test
    void sjekkOmPersonErRegistrertMedRiktigAdresse__skal_returnere_true_nar_regoppslag_returnerer_adresse() {
        boolean harAdresse = postadresseClient.sjekkOmPersonErRegistrertMedRiktigAdresse(Fnr.fraDb("09876543210"));

        assertThat(harAdresse).isTrue();

        integrasjonerMockServer.getServer().verify(
            postRequestedFor(urlPathEqualTo("/regoppslag/rest/postadresse"))
                .withRequestBody(matchingJsonPath("$.ident", equalTo("09876543210")))
        );
    }

    @Test
    void sjekkOmPersonErRegistrertMedRiktigAdresse__skal_returnere_true_nar_regoppslag_returnerer_adresse_med_kun_landkode() {
        boolean harAdresse = postadresseClient.sjekkOmPersonErRegistrertMedRiktigAdresse(Fnr.fraDb("20987654321"));

        assertThat(harAdresse).isTrue();

        integrasjonerMockServer.getServer().verify(
            postRequestedFor(urlPathEqualTo("/regoppslag/rest/postadresse"))
                .withRequestBody(matchingJsonPath("$.ident", equalTo("20987654321")))
        );
    }

    @Test
    void sjekkOmPersonErRegistrertMedRiktigAdresse__skal_returnere_false_ved_manglende_pdl_data() {
        boolean harAdresse = postadresseClient.sjekkOmPersonErRegistrertMedRiktigAdresse(Fnr.fraDb("10987654321"));

        assertThat(harAdresse).isFalse();

        integrasjonerMockServer.getServer().verify(
            postRequestedFor(urlPathEqualTo("/regoppslag/rest/postadresse"))
                .withRequestBody(matchingJsonPath("$.ident", equalTo("10987654321")))
        );
    }

    @Test
    void sjekkOmPersonErRegistrertMedRiktigAdresse__skal_returnere_false_ved_teknisk_feil_fra_regoppslag() {
        integrasjonerMockServer.getServer().stubFor(post(urlPathEqualTo("/regoppslag/rest/postadresse"))
            .withRequestBody(matchingJsonPath("$.ident", equalTo("30987654321")))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"detail\":\"Intern feil\"}")));

        boolean harAdresse = postadresseClient.sjekkOmPersonErRegistrertMedRiktigAdresse(Fnr.fraDb("30987654321"));

        assertThat(harAdresse).isFalse();

        integrasjonerMockServer.getServer().verify(
            postRequestedFor(urlPathEqualTo("/regoppslag/rest/postadresse"))
                .withRequestBody(matchingJsonPath("$.ident", equalTo("30987654321")))
        );
    }

    @Test
    void hentPostadresse__skal_kaste_funksjonell_feil_ved_manglende_data() {
        assertThatThrownBy(() -> postadresseClient.hentPostadresse(Fnr.fraDb("10987654321")))
            .isInstanceOf(RegoppslagFunctionalException.class)
            .hasMessageContaining("status=400")
            .hasMessageContaining("manglende data");

        integrasjonerMockServer.getServer().verify(
            postRequestedFor(urlPathEqualTo("/regoppslag/rest/postadresse"))
                .withRequestBody(matchingJsonPath("$.ident", equalTo("10987654321")))
        );
    }
}
