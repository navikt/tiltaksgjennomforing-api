package no.nav.tag.tiltaksgjennomforing.okonomi;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.exceptions.KontoregisterFeilException;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.CorrelationIdSupplier;
import no.nav.tag.tiltaksgjennomforing.utils.ConditionalOnPropertyNotEmpty;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@ConditionalOnPropertyNotEmpty("tiltaksgjennomforing.kontoregister.realClient")
public class KontoregisterServiceImpl implements KontoregisterService{

    private final KontoregisterProperties kontoregisterProperties;
    private final RestTemplate restTemplate;

    public KontoregisterServiceImpl(KontoregisterProperties kontoregisterProperties, @Qualifier("azure") RestTemplate restTemplate) {
        this.kontoregisterProperties = kontoregisterProperties;
        this.restTemplate = restTemplate;
    }

    public String hentKontonummer(String bedriftNr)  {
        try {
            ResponseEntity<KontoregisterResponse> response = restTemplate.exchange(new URI(String.format("%s/%s", kontoregisterProperties.getUri(),bedriftNr)),HttpMethod.GET,lagRequest(),  KontoregisterResponse.class);

            if (response.getStatusCode() != HttpStatus.OK ) {
                String feilmeldingFraTjenesten = response.getBody() != null ? response.getBody().getFeilmelding() : "";
                log.error(String.format("Kontoregister svarte med feil for bedrift : %s",bedriftNr), feilmeldingFraTjenesten);
                throw new KontoregisterFeilException();
            }

            return response.getBody().getKontonr();

        } catch (RestClientException | URISyntaxException exception) {
            log.error(String.format("Feil fra kontoregister med request-url: %s/%s", kontoregisterProperties.getUri(),bedriftNr), exception);
            throw new KontoregisterFeilException();
        }
    }

    private HttpEntity lagRequest(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Nav-consumer-Id", kontoregisterProperties.getConsumerId());
        headers.set("Nav-Call-Id", CorrelationIdSupplier.get());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity(headers);
    }
}
