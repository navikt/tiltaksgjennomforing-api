package no.nav.tag.tiltaksgjennomforing.postadresse;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.postadresse.exception.RegoppslagSecurityException;
import no.nav.tag.tiltaksgjennomforing.postadresse.exception.RegoppslagTechnicalException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * <a href="https://regoppslag.intern.dev.nav.no/swagger-ui/index.html#/Postadresse/postadresse">Regoppslag postadresse API</a>
 * Slack: #team_dokumentløsninger
 * **/
@Slf4j
@Component
public class PostadresseClient {
	private final RestTemplate azureRestTemplate;
	private final String baseUrlForRegoppslagAPI;

	public PostadresseClient(
		RestTemplate azureRestTemplate,
		RegoppslagProperties regoppslagProperties
	) {
		this.azureRestTemplate = azureRestTemplate;
		this.baseUrlForRegoppslagAPI = regoppslagProperties.getUri().toString();
	}

	public boolean sjekkOmPersonErRegistrertMedRiktigAdresse(Fnr fnr) {
		return hentPostadresseHvisTilgjengelig(fnr).isPresent();
	}

	Optional<PostadresseResponse> hentPostadresseHvisTilgjengelig(Fnr fnr) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<PostadresseRequest> request = new HttpEntity<>(PostadresseRequest.builder()
				.ident(fnr.asString())
				.build(), headers);

			PostadresseResponse response = azureRestTemplate.postForObject(
				baseUrlForRegoppslagAPI + "/postadresse",
				request,
				PostadresseResponse.class
			);
			if (response == null || response.adresse() == null) {
				log.info("Fant ikke respons/adresse fra Regoppslag. Returnerer Optional.empty().");
				return Optional.empty();
			}
			return Optional.of(response);
		} catch (RestClientResponseException exception) {
			if (exception.getStatusCode().is5xxServerError()) {
				throw new RegoppslagTechnicalException(format("Kall mot Regoppslag feilet teknisk. status=%s, body=%s", exception.getStatusCode(), exception.getResponseBodyAsString()), exception);
			}
			if (exception.getStatusCode() == UNAUTHORIZED) {
				throw new RegoppslagSecurityException(format("Kall mot Regoppslag feilet. Ingen tilgang. body=%s", exception.getResponseBodyAsString()));
			}
			log.warn("Fant ikke gyldig adresse fra Regoppslag. Returnerer Optional.empty(). status={}", exception.getStatusCode());
			return Optional.empty();
		} catch (RestClientException exception) {
			throw new RegoppslagTechnicalException(format("Kall mot Regoppslag feilet teknisk. feil=%s", exception.getMessage()), exception);
		}
	}
}
