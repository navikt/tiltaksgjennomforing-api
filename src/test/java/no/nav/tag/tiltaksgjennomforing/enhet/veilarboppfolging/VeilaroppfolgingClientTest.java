package no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles({Miljø.TEST, Miljø.WIREMOCK})
public class VeilaroppfolgingClientTest {
    @Autowired
    VeilarboppfolgingClient veilarboppfolgingClient;

    @MockBean(name = "azureRestTemplate")
    RestTemplate azureRestTemplate;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void ikkeFunnetTest() {
        when(azureRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(HentOppfolgingsstatusRespons.class)))
                .thenThrow(new HttpClientErrorException(HttpStatusCode.valueOf(404), "Oh no"));

        assertEquals(
                Optional.empty(),
                veilarboppfolgingClient.hentOppfolgingsstatus(
                        new HentOppfolgingsstatusRequest("22222222222")
                )
        );
    }

    @Test
    public void retryableTest() {
        when(azureRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(HentOppfolgingsstatusRespons.class)))
                .thenThrow(new HttpServerErrorException(HttpStatusCode.valueOf(500), "Serveren kræsja"));

        assertThrows(
                HttpServerErrorException.class,
                () -> veilarboppfolgingClient.hentOppfolgingsstatus(
                        new HentOppfolgingsstatusRequest("22222222222")
                )
        );

        Mockito.verify(azureRestTemplate, Mockito.times(3))
                .exchange(anyString(), eq(HttpMethod.POST), any(), eq(HentOppfolgingsstatusRespons.class));
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    public void cacheableTest() {
        when(azureRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(HentOppfolgingsstatusRespons.class)))
                .thenReturn(ResponseEntity.ok(new HentOppfolgingsstatusRespons(
                        new HentOppfolgingsstatusRespons.Oppfolgingsenhet("test", "test"),
                        "veileder",
                        "formidlingsgruppe",
                        "servicegruppe",
                        "hovedmaalkode"
                )));

        veilarboppfolgingClient.hentOppfolgingsstatus(
                new HentOppfolgingsstatusRequest("22222222222")
        );
        veilarboppfolgingClient.hentOppfolgingsstatus(
                new HentOppfolgingsstatusRequest("22222222222")
        );

        Mockito.verify(azureRestTemplate, Mockito.times(1))
                .exchange(anyString(), eq(HttpMethod.POST), any(), eq(HentOppfolgingsstatusRespons.class));
    }
}
