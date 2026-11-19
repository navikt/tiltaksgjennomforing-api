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
public class AoOppfolgingskontorClientTest {
    @Autowired
    AoOppfolgingskontorClient aoOppfolgingskontorClient;

    @MockitoBean(name = "azureRestTemplate")
    RestTemplate azureRestTemplate;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void ikke_funnet_gir_tom_optional() {
        when(azureRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(KontorTilhorighetGraphqlRespons.class)))
                .thenReturn(ResponseEntity.ok(respons(null)));

        assertEquals(
                Optional.empty(),
                aoOppfolgingskontorClient.hentOppfolgingskontor(new HentOppfolgingskontorRequest("22222222222"))
        );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void graphql_feil_kaster_exception() {
        when(azureRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(KontorTilhorighetGraphqlRespons.class)))
                .thenReturn(ResponseEntity.ok(new KontorTilhorighetGraphqlRespons(
                        null,
                        List.of(new KontorTilhorighetGraphqlRespons.GraphqlFeil("Ikke tilgang"))
                )));

        assertThrows(
                IllegalStateException.class,
                () -> aoOppfolgingskontorClient.hentOppfolgingskontor(new HentOppfolgingskontorRequest("22222222222"))
        );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void retryable_test() {
        when(azureRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(KontorTilhorighetGraphqlRespons.class)))
                .thenThrow(new HttpServerErrorException(HttpStatusCode.valueOf(500), "Serveren kræsja"));

        assertThrows(
                HttpServerErrorException.class,
                () -> aoOppfolgingskontorClient.hentOppfolgingskontor(new HentOppfolgingskontorRequest("22222222222"))
        );

        Mockito.verify(azureRestTemplate, Mockito.times(3))
                .exchange(anyString(), eq(HttpMethod.POST), any(), eq(KontorTilhorighetGraphqlRespons.class));
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    public void cacheable_test() {
        when(azureRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(KontorTilhorighetGraphqlRespons.class)))
                .thenReturn(ResponseEntity.ok(respons(new Oppfolgingskontor("0906", "Nav Test", Oppfolgingskontor.KontorType.ARBEIDSOPPFOLGING))));

        aoOppfolgingskontorClient.hentOppfolgingskontor(new HentOppfolgingskontorRequest("22222222222"));
        Optional<Oppfolgingskontor> kontor = aoOppfolgingskontorClient.hentOppfolgingskontor(new HentOppfolgingskontorRequest("22222222222"));

        assertEquals("0906", kontor.orElseThrow().kontorId());
        Mockito.verify(azureRestTemplate, Mockito.times(1))
                .exchange(anyString(), eq(HttpMethod.POST), any(), eq(KontorTilhorighetGraphqlRespons.class));
    }

    private static KontorTilhorighetGraphqlRespons respons(Oppfolgingskontor kontor) {
        return new KontorTilhorighetGraphqlRespons(
                new KontorTilhorighetGraphqlRespons.Data(kontor),
                null
        );
    }
}
