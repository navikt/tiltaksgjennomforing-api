package no.nav.tag.tiltaksgjennomforing.integrasjon.sts;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import no.nav.tag.tiltaksgjennomforing.integrasjon.sts.STSClient;
import no.nav.tag.tiltaksgjennomforing.integrasjon.sts.STSToken;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class STSClientTest {

    @Mock
    private RestTemplate restTemplate;
    
    @Before
    public void setUp() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(STSToken.class)))
                .thenReturn(ResponseEntity.ok().build());
    }

    @Test
    public void getToken__skal_gjøre_kall_med_riktige_parametre() {
        STSClient stsClient = new STSClient(restTemplate, "https://test.no");

        stsClient.hentSTSToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        verify(restTemplate).exchange(
                eq("https://test.no/sts/token?grant_type=client_credentials&scope=openid"),
                eq(HttpMethod.GET),
                eq(entity),
                eq(STSToken.class)

        );
    }

}
