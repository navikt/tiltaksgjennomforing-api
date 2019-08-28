package no.nav.tag.tiltaksgjennomforing.integrasjon.sts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class STSClient {

    private final RestTemplate stsBasicAuthRestTemplate;
    private final String stsUrl;
    
    public STSClient(
            RestTemplate stsBasicAuthRestTemplate,
            @Value("${tiltaksgjennomforing.sts.sts-uri}") String stsUrl
    ) {
        this.stsBasicAuthRestTemplate = stsBasicAuthRestTemplate;
        this.stsUrl = stsUrl;
    }

    public STSToken hentSTSToken() {
        String uriString = UriComponentsBuilder.fromHttpUrl(stsUrl + "/sts/token")
                .queryParam("grant_type", "client_credentials")
                .queryParam("scope", "openid")
                .toUriString();

        return stsBasicAuthRestTemplate.exchange(
                uriString,
                HttpMethod.GET,
                getRequestEntity(),
                STSToken.class
        ).getBody();

    }

    private HttpEntity<String> getRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(headers);
    }

}
