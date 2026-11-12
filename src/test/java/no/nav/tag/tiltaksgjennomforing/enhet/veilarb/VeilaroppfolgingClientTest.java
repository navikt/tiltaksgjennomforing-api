package no.nav.tag.tiltaksgjennomforing.enhet.veilarb;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles({Miljø.TEST})
public class VeilaroppfolgingClientTest {
    @Autowired
    VeilarboppfolgingClient veilarboppfolgingClient;

    @MockitoBean(name = "azureRestTemplate")
    RestTemplate azureRestTemplate;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void ikke_funnet_gir_tom_optional() {
        when(azureRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(OppfolgingsenhetGraphqlRespons.class)))
                .thenReturn(ResponseEntity.ok(respons(null)));

        assertEquals(
                Optional.empty(),
                veilarboppfolgingClient.hentOppfolgingsenhet(new HentOppfolgingsenhetRequest("22222222222"))
        );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void graphql_feil_kaster_exception() {
        when(azureRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(OppfolgingsenhetGraphqlRespons.class)))
                .thenReturn(ResponseEntity.ok(new OppfolgingsenhetGraphqlRespons(
                        null,
                        List.of(new OppfolgingsenhetGraphqlRespons.GraphqlFeil("Ikke tilgang"))
                )));

        assertThrows(
                IllegalStateException.class,
                () -> veilarboppfolgingClient.hentOppfolgingsenhet(new HentOppfolgingsenhetRequest("22222222222"))
        );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void retryable_test() {
        when(azureRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(OppfolgingsenhetGraphqlRespons.class)))
                .thenThrow(new HttpServerErrorException(HttpStatusCode.valueOf(500), "Serveren kræsja"));

        assertThrows(
                HttpServerErrorException.class,
                () -> veilarboppfolgingClient.hentOppfolgingsenhet(new HentOppfolgingsenhetRequest("22222222222"))
        );

        Mockito.verify(azureRestTemplate, Mockito.times(3))
                .exchange(anyString(), eq(HttpMethod.POST), any(), eq(OppfolgingsenhetGraphqlRespons.class));
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    public void cacheable_test() {
        when(azureRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(OppfolgingsenhetGraphqlRespons.class)))
                .thenReturn(ResponseEntity.ok(respons(new Oppfolgingsenhet("0906", "Nav Test", "ARENA"))));

        veilarboppfolgingClient.hentOppfolgingsenhet(new HentOppfolgingsenhetRequest("22222222222"));
        Optional<Oppfolgingsenhet> enhet = veilarboppfolgingClient.hentOppfolgingsenhet(new HentOppfolgingsenhetRequest("22222222222"));

        assertEquals("0906", enhet.orElseThrow().id());
        Mockito.verify(azureRestTemplate, Mockito.times(1))
                .exchange(anyString(), eq(HttpMethod.POST), any(), eq(OppfolgingsenhetGraphqlRespons.class));
    }

    private static OppfolgingsenhetGraphqlRespons respons(Oppfolgingsenhet enhet) {
        return new OppfolgingsenhetGraphqlRespons(
                new OppfolgingsenhetGraphqlRespons.Data(new OppfolgingsenhetGraphqlRespons.OppfolgingsEnhetsInfo(enhet)),
                null
        );
    }
}
