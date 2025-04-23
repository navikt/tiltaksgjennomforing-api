package no.nav.tag.tiltaksgjennomforing.okonomi;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.exceptions.KontoregisterFantIkkeBedriftFeilException;
import no.nav.tag.tiltaksgjennomforing.exceptions.KontoregisterFeilException;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.CorrelationIdSupplier;
import no.nav.tag.tiltaksgjennomforing.utils.ConditionalOnPropertyNotEmpty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@Slf4j
@ConditionalOnPropertyNotEmpty("tiltaksgjennomforing.kontoregister.realClient")
public class KontoregisterServiceImpl implements KontoregisterService {

    private final RestTemplate azureRestTemplate;
    private final String baseUrl;
    private final String navConsumerId;

    public KontoregisterServiceImpl(
        RestTemplate azureRestTemplate,
        KontoregisterProperties kontoregisterProperties
    ) {
        this.azureRestTemplate = azureRestTemplate;
        this.baseUrl = kontoregisterProperties.getUri();
        this.navConsumerId = kontoregisterProperties.getConsumerId();
    }

    public String hentKontonummer(String bedriftNr) {
        try {
            ResponseEntity<KontoregisterResponse> response = azureRestTemplate.exchange(
                baseUrl + "/{bedriftNr}",
                HttpMethod.GET,
                lagRequest(),
                KontoregisterResponse.class,
                Map.of("bedriftNr", bedriftNr)
            );
            return response.getBody().getKontonr();

        } catch (RestClientException exception) {
            if (exception instanceof HttpClientErrorException hcee) {
                if (hcee.getStatusCode() == NOT_FOUND) {
                    throw new KontoregisterFantIkkeBedriftFeilException();
                }
            }
            log.error(
                "Feil fra kontoregister for bedriftNr={}",
                bedriftNr,
                exception
            );
            throw new KontoregisterFeilException();
        }
    }

    private HttpEntity<String> lagRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Nav-consumer-Id", navConsumerId);
        headers.set("Nav-Call-Id", CorrelationIdSupplier.get());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }
}
