package no.nav.tag.tiltaksgjennomforing.postadresse;

import no.nav.tag.tiltaksgjennomforing.IntegrasjonerMockServer;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.postadresse.exception.RegoppslagFunctionalException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles({ Miljø.TEST, Miljø.WIREMOCK })
@DirtiesContext
class PostadresseConsumerTest {

    @Autowired
    private PostadresseConsumer postadresseConsumer;

    @Autowired
    private IntegrasjonerMockServer integrasjonerMockServer;

    @Test
    void hentAdresse__skal_returnere_adresse_fra_regoppslag() {
        Adresse adresse = postadresseConsumer.hentAdresse(
            PostadresseRequest.builder()
                .ident("09876543210")
                .filtrerAdressebeskyttelse(Set.of())
                .build()
        );

        assertThat(adresse).isNotNull();
        assertThat(adresse.adresselinje1()).isEqualTo("eksempelveien 23 A");
        assertThat(adresse.adresselinje2()).isEqualTo("eksempelveien 24 A");
        assertThat(adresse.adresselinje3()).isEqualTo("eksempelveien 25 A");
        assertThat(adresse.postnummer()).isEqualTo("1337");
        assertThat(adresse.poststed()).isEqualTo("poststed");
        assertThat(adresse.landkode()).isEqualTo("NO");

        integrasjonerMockServer.getServer().verify(
            postRequestedFor(urlPathEqualTo("/regoppslag/rest/postadresse"))
                .withRequestBody(matchingJsonPath("$.ident", equalTo("09876543210")))
        );
    }

    @Test
    void hentAdresse__skal_returnere_adresse_med_kun_landkode_fra_regoppslag() {
        Adresse adresse = postadresseConsumer.hentAdresse(
            PostadresseRequest.builder()
                .ident("20987654321")
                .filtrerAdressebeskyttelse(Set.of())
                .build()
        );

        assertThat(adresse).isNotNull();
        assertThat(adresse.adresselinje1()).isNull();
        assertThat(adresse.adresselinje2()).isNull();
        assertThat(adresse.adresselinje3()).isNull();
        assertThat(adresse.postnummer()).isNull();
        assertThat(adresse.poststed()).isNull();
        assertThat(adresse.landkode()).isEqualTo("NO");

        integrasjonerMockServer.getServer().verify(
            postRequestedFor(urlPathEqualTo("/regoppslag/rest/postadresse"))
                .withRequestBody(matchingJsonPath("$.ident", equalTo("20987654321")))
        );
    }

    @Test
    void hentAdresse__skal_kaste_funksjonell_feil_ved_manglende_pdl_data() {
        PostadresseRequest postadresseRequest = PostadresseRequest.builder()
            .ident("10987654321")
            .filtrerAdressebeskyttelse(Set.of())
            .build();

        assertThatThrownBy(() -> postadresseConsumer.hentAdresse(postadresseRequest))
            .isInstanceOf(RegoppslagFunctionalException.class)
            .hasMessageContaining("status=400")
            .hasMessageContaining("manglende data i PDL");

        integrasjonerMockServer.getServer().verify(
            postRequestedFor(urlPathEqualTo("/regoppslag/rest/postadresse"))
                .withRequestBody(matchingJsonPath("$.ident", equalTo("10987654321")))
        );
    }
}
