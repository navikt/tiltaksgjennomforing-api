package no.nav.tag.tiltaksgjennomforing.digitalkommunikasjon;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KrrClientTest {

    private MockRestServiceServer server;
    private KrrClient krrClient;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);

        KrrProperties krrProperties = new KrrProperties();
        krrProperties.setUri(URI.create("https://krr.example"));

        krrClient = new KrrClient(restTemplate, krrProperties);
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
                      "reservert": true
                    }
                  }
                }
                """, MediaType.APPLICATION_JSON));

        assertThat(krrClient.hentPersonReservertForDigitalKontakt(fnr)).contains(true);
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

        assertThat(krrClient.hentPersonReservertForDigitalKontakt(fnr)).isEmpty();
        server.verify();
    }
}
