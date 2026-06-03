package no.nav.tag.tiltaksgjennomforing.brev.digitalkontaktinformasjon;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Digdir sitt Kontakt- og reservasjonsregister (KRR) inneholder informasjon
 * om hvordan innbyggere ønsker å bli kontaktet av det offentlige, og
 * om de har reservert seg mot digital kommunikasjon.
 *
 * Klient for Digdir KRR-proxy.
 *
 * @see <a href="https://docs.digdir.no/docs/Kontaktregisteret/krr_attributter#kodeverk-for-varslingsstatus">KRR kodeverk for varslingsstatus</a>
 * @see <a href="https://digdir-krr-proxy.intern.dev.nav.no/swagger-ui/index.html">Swagger</a>
 * @see <a href="https://github.com/navikt/digdir-krr">digdir-krr</a>
 */
@Slf4j
@Component
public class DigitalKontaktinformasjonClient {

    private static final String KRR_PERSON_ENDEPUNKT = "/rest/v1/personer";

    private final RestTemplate azureRestTemplate;
    private final URI baseUri;

    public DigitalKontaktinformasjonClient(RestTemplate azureRestTemplate,
                                           DigitalkontaktInfoProperties digitalkontaktInfoProperties
    ) {
        this.azureRestTemplate = azureRestTemplate;
        this.baseUri = Objects.requireNonNull(digitalkontaktInfoProperties.getUri(), "KRR URI må være konfigurert");
    }

    public boolean erPersonReservertMotDigitalKontakt(Fnr fnr) {
        return hentErPersonReservertMotDigitalKontakt(fnr).orElse(false);
    }

    Optional<Boolean> hentErPersonReservertMotDigitalKontakt(Fnr fnr) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PersonKontaktinformasjonRequest> request = new HttpEntity<>(
                new PersonKontaktinformasjonRequest(List.of(fnr.asString())),
                headers
            );

            ResponseEntity<PersonKontaktinformasjonRespons> respons = azureRestTemplate.exchange(
                baseUri + KRR_PERSON_ENDEPUNKT,
                HttpMethod.POST,
                request,
                PersonKontaktinformasjonRespons.class
            );

            if (!respons.getStatusCode().is2xxSuccessful()) {
                if (respons.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                    log.warn("Fant ikke person i KRR. Returnerer Optional.empty(). status={}", respons.getStatusCode());
                    return Optional.empty();
                }
                throw new RestClientException("KRR svarte med uventet status " + respons.getStatusCode());
            }

            return Optional.ofNullable(respons.getBody())
                .map(PersonKontaktinformasjonRespons::personer)
                .map(personer -> personer.get(fnr.asString()))
                .map(Kontaktinfo::erReservertForDigitalKontakt);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                log.warn("Fant ikke person i KRR. Returnerer Optional.empty(). status={}", e.getStatusCode());
                return Optional.empty();
            }
            log.warn("HTTP-feil ved henting av digital kontaktreservasjon fra KRR. status={}", e.getStatusCode());
            throw e;
        } catch (RestClientException e) {
            log.warn("Feil ved henting av digital kontaktreservasjon fra KRR", e);
            throw e;
        }
    }
}
