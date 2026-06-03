package no.nav.tag.tiltaksgjennomforing.brev.digitalkontaktinformasjon;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.brev.digitalkontaktinformasjon.DigitalKontaktinformasjonClient;
import no.nav.tag.tiltaksgjennomforing.brev.digitalkontaktinformasjon.DigitalkontaktInfoProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DigitalKontaktinformasjonClientTest {

    private MockRestServiceServer server;
    private DigitalKontaktinformasjonClient digitalKontaktinformasjonClient;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);

        DigitalkontaktInfoProperties digitalkontaktInfoProperties = new DigitalkontaktInfoProperties();
        digitalkontaktInfoProperties.setUri(URI.create("https://krr.example"));

        digitalKontaktinformasjonClient = new DigitalKontaktinformasjonClient(restTemplate,
            digitalkontaktInfoProperties
        );
    }

    @Test
    void henterOmPersonErReservertForDigitalKontakt() {
        Fnr fnr = Fnr.fraDb("12345678910");

        server.expect(requestTo("https://krr.example/rest/v1/personer"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json("""
                {
                  "personidenter": ["12345678910"]
                }
                """))
            .andRespond(withSuccess("""
                {
                  "personer": {
                    "12345678910": {
                      "kanVarsles": false,
                      "reservert": true
                    }
                  }
                }
                """, MediaType.APPLICATION_JSON));

        assertThat(digitalKontaktinformasjonClient.hentErPersonReservertMotDigitalKontakt(fnr)).contains(true);
        server.verify();
    }

    @Test
    void returnererIkkeReservertNarPersonKanVarsles() {
        Fnr fnr = Fnr.fraDb("12345678910");

        server.expect(requestTo("https://krr.example/rest/v1/personer"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("""
                {
                  "personer": {
                    "12345678910": {
                      "kanVarsles": true
                    }
                  }
                }
                """, MediaType.APPLICATION_JSON));

        assertThat(digitalKontaktinformasjonClient.hentErPersonReservertMotDigitalKontakt(fnr)).contains(false);
        server.verify();
    }

    @Test
    void returnererFalseFraPublicMetodeNarPersonIkkeFinnesIResponsen() {
        Fnr fnr = Fnr.fraDb("12345678910");

        server.expect(requestTo("https://krr.example/rest/v1/personer"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("""
                {
                  "personer": {}
                }
                """, MediaType.APPLICATION_JSON));

        assertThat(digitalKontaktinformasjonClient.erPersonReservertMotDigitalKontakt(fnr)).isFalse();
        server.verify();
    }

    @Test
    void returnererTrueFraPublicMetodeNarPersonErReservertForDigitalKontakt() {
        Fnr fnr = Fnr.fraDb("12345678910");

        server.expect(requestTo("https://krr.example/rest/v1/personer"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("""
                {
                  "personer": {
                    "12345678910": {
                      "kanVarsles": false,
                      "reservert": true
                    }
                  }
                }
                """, MediaType.APPLICATION_JSON));

        assertThat(digitalKontaktinformasjonClient.erPersonReservertMotDigitalKontakt(fnr)).isTrue();
        server.verify();
    }

    @Test
    void returnererFalseFraPublicMetodeNarPersonIkkeErReservertForDigitalKontakt() {
        Fnr fnr = Fnr.fraDb("12345678910");

        server.expect(requestTo("https://krr.example/rest/v1/personer"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("""
                {
                  "personer": {
                    "12345678910": {
                      "kanVarsles": true,
                      "reservert": false
                    }
                  }
                }
                """, MediaType.APPLICATION_JSON));

        assertThat(digitalKontaktinformasjonClient.erPersonReservertMotDigitalKontakt(fnr)).isFalse();
        server.verify();
    }

    @Test
    void returnererTomOptionalNarPersonIkkeFinnesIResponsen() {
        Fnr fnr = Fnr.fraDb("12345678910");

        server.expect(requestTo("https://krr.example/rest/v1/personer"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("""
                {
                  "personer": {}
                }
                """, MediaType.APPLICATION_JSON));

        assertThat(digitalKontaktinformasjonClient.hentErPersonReservertMotDigitalKontakt(fnr)).isEmpty();
        server.verify();
    }

    @Test
    void returnererTomOptionalNarKrrSvarerMedIkkeFunnet() {
        Fnr fnr = Fnr.fraDb("12345678910");

        server.expect(requestTo("https://krr.example/rest/v1/personer"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(digitalKontaktinformasjonClient.hentErPersonReservertMotDigitalKontakt(fnr)).isEmpty();
        server.verify();
    }

    @Test
    void kasterExceptionNarKrrSvarerMedUgyldigForesporsel() {
        Fnr fnr = Fnr.fraDb("12345678910");

        server.expect(requestTo("https://krr.example/rest/v1/personer"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> digitalKontaktinformasjonClient.hentErPersonReservertMotDigitalKontakt(fnr))
            .isInstanceOf(RestClientResponseException.class);
        server.verify();
    }

    @Test
    void kasterExceptionNarKrrSvarerMedServerfeil() {
        Fnr fnr = Fnr.fraDb("12345678910");

        server.expect(requestTo("https://krr.example/rest/v1/personer"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> digitalKontaktinformasjonClient.hentErPersonReservertMotDigitalKontakt(fnr))
            .isInstanceOf(RestClientResponseException.class);
        server.verify();
    }
}
