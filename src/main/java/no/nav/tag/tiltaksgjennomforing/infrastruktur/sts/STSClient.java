package no.nav.tag.tiltaksgjennomforing.infrastruktur.sts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
class STSClient {

    private final RestTemplate noAuthRestTemplate;
    private final String stsUriString;

    public STSClient(StsProperties stsProperties) {
        this.noAuthRestTemplate = new RestTemplateBuilder()
            .basicAuthentication(stsProperties.getUsername(), stsProperties.getPassword())
            .build();
        this.stsUriString = UriComponentsBuilder.fromUri(stsProperties.getRestUri())
            .queryParam("grant_type", "client_credentials")
            .queryParam("scope", "openid")
            .toUriString();
    }

    public STSToken hentSTSToken() {
        return noAuthRestTemplate.exchange(
            stsUriString,
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
