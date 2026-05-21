package no.nav.tag.tiltaksgjennomforing.postadresse;

import com.github.tomakehurst.wiremock.http.Fault;
import no.nav.tag.tiltaksgjennomforing.IntegrasjonerMockServer;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.postadresse.exception.RegoppslagTechnicalException;
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
    void hentPostadresseHvisTilgjengelig__skal_returnere_hele_responsen_fra_regoppslag() {
        var response = postadresseClient.hentPostadresseHvisTilgjengelig(Fnr.fraDb("09876543210"));

        assertThat(response).isPresent();
        assertThat(response.get().navn()).isEqualTo("Jan Neimansen");
        assertThat(response.get().adresse()).isNotNull();
        assertThat(response.get().adresse().adresselinje1()).isEqualTo("eksempelveien 23 A");

        integrasjonerMockServer.getServer().verify(
            postRequestedFor(urlPathEqualTo("/regoppslag/rest/postadresse"))
                .withRequestBody(matchingJsonPath("$.ident", equalTo("09876543210")))
        );
    }

    @Test
    void hentPostadresseHvisTilgjengelig__skal_returnere_empty_nar_regoppslag_returnerer_null_adresse() {
        integrasjonerMockServer.getServer().stubFor(post(urlPathEqualTo("/regoppslag/rest/postadresse"))
            .withRequestBody(matchingJsonPath("$.ident", equalTo("50987654321")))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"navn\":\"Jan Neimansen\",\"adresse\":null}")));

        var response = postadresseClient.hentPostadresseHvisTilgjengelig(Fnr.fraDb("50987654321"));

        assertThat(response).isEmpty();

        integrasjonerMockServer.getServer().verify(
            postRequestedFor(urlPathEqualTo("/regoppslag/rest/postadresse"))
                .withRequestBody(matchingJsonPath("$.ident", equalTo("50987654321")))
        );
    }

    @Test
    void hentPostadresseHvisTilgjengelig__skal_returnere_empty_nar_regoppslag_returnerer_tom_respons() {
        integrasjonerMockServer.getServer().stubFor(post(urlPathEqualTo("/regoppslag/rest/postadresse"))
            .withRequestBody(matchingJsonPath("$.ident", equalTo("60987654321")))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("")));

        var response = postadresseClient.hentPostadresseHvisTilgjengelig(Fnr.fraDb("60987654321"));

        assertThat(response).isEmpty();

        integrasjonerMockServer.getServer().verify(
            postRequestedFor(urlPathEqualTo("/regoppslag/rest/postadresse"))
                .withRequestBody(matchingJsonPath("$.ident", equalTo("60987654321")))
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
    void sjekkOmPersonErRegistrertMedRiktigAdresse__skal_returnere_false_nar_regoppslag_returnerer_null_adresse() {
        integrasjonerMockServer.getServer().stubFor(post(urlPathEqualTo("/regoppslag/rest/postadresse"))
            .withRequestBody(matchingJsonPath("$.ident", equalTo("40987654321")))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"navn\":\"Jan Neimansen\",\"adresse\":null}")));

        boolean harAdresse = postadresseClient.sjekkOmPersonErRegistrertMedRiktigAdresse(Fnr.fraDb("40987654321"));

        assertThat(harAdresse).isFalse();

        integrasjonerMockServer.getServer().verify(
            postRequestedFor(urlPathEqualTo("/regoppslag/rest/postadresse"))
                .withRequestBody(matchingJsonPath("$.ident", equalTo("40987654321")))
        );
    }

    @Test
    void sjekkOmPersonErRegistrertMedRiktigAdresse__skal_returnere_false_nar_regoppslag_returnerer_tom_respons() {
        integrasjonerMockServer.getServer().stubFor(post(urlPathEqualTo("/regoppslag/rest/postadresse"))
            .withRequestBody(matchingJsonPath("$.ident", equalTo("70987654321")))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("")));

        boolean harAdresse = postadresseClient.sjekkOmPersonErRegistrertMedRiktigAdresse(Fnr.fraDb("70987654321"));

        assertThat(harAdresse).isFalse();

        integrasjonerMockServer.getServer().verify(
            postRequestedFor(urlPathEqualTo("/regoppslag/rest/postadresse"))
                .withRequestBody(matchingJsonPath("$.ident", equalTo("70987654321")))
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
    void sjekkOmPersonErRegistrertMedRiktigAdresse__skal_kaste_teknisk_feil_ved_5xx_fra_regoppslag() {
        integrasjonerMockServer.getServer().stubFor(post(urlPathEqualTo("/regoppslag/rest/postadresse"))
            .withRequestBody(matchingJsonPath("$.ident", equalTo("30987654321")))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"detail\":\"Intern feil\"}")));

        assertThatThrownBy(() -> postadresseClient.sjekkOmPersonErRegistrertMedRiktigAdresse(Fnr.fraDb("30987654321")))
            .isInstanceOf(RegoppslagTechnicalException.class)
            .hasMessageContaining("status=500")
            .hasCauseInstanceOf(Exception.class);

        integrasjonerMockServer.getServer().verify(
            postRequestedFor(urlPathEqualTo("/regoppslag/rest/postadresse"))
                .withRequestBody(matchingJsonPath("$.ident", equalTo("30987654321")))
        );
    }

    @Test
    void sjekkOmPersonErRegistrertMedRiktigAdresse__skal_kaste_teknisk_feil_ved_resttemplate_feil_uten_http_status() {
        integrasjonerMockServer.getServer().stubFor(post(urlPathEqualTo("/regoppslag/rest/postadresse"))
            .withRequestBody(matchingJsonPath("$.ident", equalTo("80987654321")))
            .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        assertThatThrownBy(() -> postadresseClient.sjekkOmPersonErRegistrertMedRiktigAdresse(Fnr.fraDb("80987654321")))
            .isInstanceOf(RegoppslagTechnicalException.class)
            .hasMessageContaining("Kall mot Regoppslag feilet teknisk")
            .hasCauseInstanceOf(Exception.class);

        integrasjonerMockServer.getServer().verify(
            postRequestedFor(urlPathEqualTo("/regoppslag/rest/postadresse"))
                .withRequestBody(matchingJsonPath("$.ident", equalTo("80987654321")))
        );
    }

    @Test
    void hentPostadresseHvisTilgjengelig__skal_kaste_teknisk_feil_ved_resttemplate_feil_uten_http_status() {
        integrasjonerMockServer.getServer().stubFor(post(urlPathEqualTo("/regoppslag/rest/postadresse"))
            .withRequestBody(matchingJsonPath("$.ident", equalTo("90987654321")))
            .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        assertThatThrownBy(() -> postadresseClient.hentPostadresseHvisTilgjengelig(Fnr.fraDb("90987654321")))
            .isInstanceOf(RegoppslagTechnicalException.class)
            .hasMessageContaining("Kall mot Regoppslag feilet teknisk")
            .hasCauseInstanceOf(Exception.class);

        integrasjonerMockServer.getServer().verify(
            postRequestedFor(urlPathEqualTo("/regoppslag/rest/postadresse"))
                .withRequestBody(matchingJsonPath("$.ident", equalTo("90987654321")))
        );
    }

    @Test
    void hentPostadresseHvisTilgjengelig__skal_returnere_empty_ved_manglende_data() {
        var response = postadresseClient.hentPostadresseHvisTilgjengelig(Fnr.fraDb("10987654321"));

        assertThat(response).isEmpty();

        integrasjonerMockServer.getServer().verify(
            postRequestedFor(urlPathEqualTo("/regoppslag/rest/postadresse"))
                .withRequestBody(matchingJsonPath("$.ident", equalTo("10987654321")))
        );
    }
}
