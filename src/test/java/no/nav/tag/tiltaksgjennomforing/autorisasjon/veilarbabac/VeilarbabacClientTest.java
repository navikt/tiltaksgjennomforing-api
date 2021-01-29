package no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac;

import static no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.VeilarbabacClient.DENY_RESPONSE;
import static no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.VeilarbabacClient.PERMIT_RESPONSE;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.sts.STSClient;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.sts.STSToken;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class VeilarbabacClientTest {

    private static final String FNR = "11111111111";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private STSClient stsClient;

    private VeilarbabacClient veilarbabacClient;

    @Before
    public void setUp() {
        mockReturverdiFraVeilarbabac("permit");
        when(stsClient.hentSTSToken()).thenReturn(etStsToken());
        veilarbabacClient = new VeilarbabacClient(
                restTemplate,
                stsClient,
                "https://test.no"
        );
    }

    private static STSToken etStsToken() {
        return new STSToken("-", "-", 100);
    }

    @Test
    public void harSkrivetilgangTilKandidat__skal_returnere_false_hvis_deny() {
        mockReturverdiFraVeilarbabac(DENY_RESPONSE);
        assertThat(veilarbabacClient.sjekkTilgang("1000000000001", TilgangskontrollAction.update, enVeileder().getIdentifikator())).isFalse();
    }

    private void mockReturverdiFraVeilarbabac(String response) {
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok().body(response));
    }

    private static InnloggetVeileder enVeileder() {
        return new InnloggetVeileder(new NavIdent("X123456"), Collections.emptySet());
    }

    @Test
    public void harSkrivetilgangTilKandidat__skal_returnere_true_hvis_permit() {
        mockReturverdiFraVeilarbabac(PERMIT_RESPONSE);
        assertThat(veilarbabacClient.sjekkTilgang(FNR, TilgangskontrollAction.update, enVeileder().getIdentifikator())).isTrue();
    }

    @Test(expected=TilgangskontrollException.class)
    public void harSkrivetilgangTilKandidat__skal_kaste_exception_hvis_ikke_allow_eller_deny() {
        mockReturverdiFraVeilarbabac("blabla");
        veilarbabacClient.sjekkTilgang(FNR, TilgangskontrollAction.update, enVeileder().getIdentifikator());
    }

    @Test
    public void harSkrivetilgangTilKandidat__skal_gjøre_kall_med_riktige_parametre() {
        STSToken stsToken = etStsToken();

        InnloggetVeileder veileder = enVeileder();

        when(stsClient.hentSTSToken()).thenReturn(stsToken);

        veilarbabacClient.sjekkTilgang(FNR, TilgangskontrollAction.update, enVeileder().getIdentifikator());

        HttpHeaders headers = new HttpHeaders();
        headers.set("subject", veileder.getIdentifikator().asString());
        headers.set("subjectType", "InternBruker");
        headers.set("Authorization", "Bearer " + stsToken.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        verify(restTemplate).exchange(
                eq("https://test.no/person?fnr=" + FNR + "&action=update"),
                eq(HttpMethod.GET),
                eq(new HttpEntity(headers)),
                eq(String.class)
        );
    }

}
