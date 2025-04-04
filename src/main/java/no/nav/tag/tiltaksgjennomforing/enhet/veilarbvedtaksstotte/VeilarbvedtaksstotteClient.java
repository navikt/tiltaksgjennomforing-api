package no.nav.tag.tiltaksgjennomforing.enhet.veilarbvedtaksstotte;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Component
public class VeilarbvedtaksstotteClient {
    private final RestTemplate restTemplate;
    private final VeilarbvedtaksstøtteProperties veilarbvedtaksstotteProperties;

    public VeilarbvedtaksstotteClient(RestTemplate azureRestTemplate, VeilarbvedtaksstøtteProperties veilarbvedtaksstotteProperties) {
        this.restTemplate = azureRestTemplate;
        this.veilarbvedtaksstotteProperties = veilarbvedtaksstotteProperties;
    }

    public Optional<Gjeldende14aVedtakResponse> hentGjeldende14aVedtak(Gjeldende14aVedtakRequest gjeldende14aVedtakRequest) {
        log.info("Henter 14-a vedtak fra veilarbvedtaksstotte");

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set("Nav-Consumer-Id", "tiltaksgjennomforing-api");

        try {
            String url = veilarbvedtaksstotteProperties.getUrl() + "/veilarbvedtaksstotte/api/ekstern/hent-gjeldende-14a-vedtak";
            Gjeldende14aVedtakResponse gjeldende14aVedtakResponse = restTemplate.postForObject(url, gjeldende14aVedtakRequest, Gjeldende14aVedtakResponse.class);
            return Optional.ofNullable(gjeldende14aVedtakResponse);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}
