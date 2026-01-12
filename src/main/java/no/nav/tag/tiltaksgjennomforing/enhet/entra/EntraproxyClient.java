package no.nav.tag.tiltaksgjennomforing.enhet.entra;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
class EntraproxyClient {
    private final String baseUrl;
    private final RestTemplate azureRestTemplate;

    public EntraproxyClient(
        EntraproxyProperties properties,
        RestTemplate azureRestTemplate
    ) {
        this.baseUrl = properties.getUri().toString();
        this.azureRestTemplate = azureRestTemplate;
    }

    public List<EntraproxyEnhet> hentEnheterNavAnsattHarTilgangTil(NavIdent ident) {
        try {
            ResponseEntity<List<EntraproxyEnhet>> respons = azureRestTemplate.exchange(
                baseUrl + "/api/v1/enhet/ansatt/{navIdent}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {},
                Map.of("navIdent", ident.asString())
            );

            if (respons.getBody() == null) {
                log.warn("Tom liste. Fant ingen enheter for ident {}", ident);
                return List.of();
            }

            return respons.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Ident {} ble ikke funnet i Entra", ident, e);
            return List.of();
        } catch (RestClientException e) {
            log.warn("Feil ved henting av enheter for ident {}", ident, e);
            throw e;
        }
    }
}
