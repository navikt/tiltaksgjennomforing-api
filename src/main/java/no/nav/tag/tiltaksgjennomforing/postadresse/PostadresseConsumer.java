package no.nav.tag.tiltaksgjennomforing.postadresse;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.postadresse.exception.PersonErDoedUkjentAdresseException;
import no.nav.tag.tiltaksgjennomforing.postadresse.exception.RegoppslagFunctionalException;
import no.nav.tag.tiltaksgjennomforing.postadresse.exception.RegoppslagSecurityException;
import no.nav.tag.tiltaksgjennomforing.postadresse.exception.RegoppslagTechnicalException;
import no.nav.tag.tiltaksgjennomforing.postadresse.exception.UkjentAdresseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.GONE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * <a href="https://regoppslag.intern.dev.nav.no/swagger-ui/index.html#/Postadresse/postadresse">Regoppslag postadresse API</a>
 * Slack: #team_dokumentløsninger
 * **/
@Slf4j
@Component
public class PostadresseConsumer {
	private static final String BEHANDLINGSNUMMER_HEADER = "behandlingsnummer";
	private static final String POSTADRESSE_BEHANDLINGSNUMMER = "B662";

	private final RestTemplate azureRestTemplate;
	private final String baseUrl;
	private final ObjectMapper objectMapper;

	public PostadresseConsumer(
		RestTemplate azureRestTemplate,
		RegoppslagProperties regoppslagProperties,
		ObjectMapper objectMapper
	) {
		this.azureRestTemplate = azureRestTemplate;
		this.baseUrl = regoppslagProperties.getUri().toString();
		this.objectMapper = objectMapper;
	}

	public Adresse hentAdresse(PostadresseRequest postadresseRequest) {
		return hentPostadresse(postadresseRequest).adresse();
	}

	public PostadresseResponse hentPostadresse(PostadresseRequest postadresseRequest) {
		try {
			log.info("hent-postadresse baseUrl: {}", baseUrl);
			log.info("hent-postadresse request: {}", postadresseRequest);
			PostadresseResponse response = azureRestTemplate.postForObject(
				baseUrl + "/postadresse",
				lagRequest(postadresseRequest),
				PostadresseResponse.class
			);
			log.info("hent-postadresse response: {}", response);
			if (response == null) {
				throw new RegoppslagTechnicalException("Kall mot Regoppslag returnerte tom respons.");
			}
			Adresse adresse = response.adresse();
			if (adresse == null) {
				throw new RegoppslagTechnicalException("Kall mot Regoppslag returnerte tom adresse.");
			}
			return response;
		} catch (RestClientResponseException exception) {
			throw mapRegoppslagException(exception);
		}
	}

	private HttpEntity<PostadresseRequest> lagRequest(PostadresseRequest postadresseRequest) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(BEHANDLINGSNUMMER_HEADER, POSTADRESSE_BEHANDLINGSNUMMER);
		return new HttpEntity<>(postadresseRequest, headers);
	}

	private RuntimeException mapRegoppslagException(RestClientResponseException exception) {
		if (exception.getStatusCode().is5xxServerError()) {
			return new RegoppslagTechnicalException(format("Kall mot Regoppslag feilet teknisk. status=%s, problemDetail=%s", exception.getStatusCode(), lesProblemDetail(exception)));
		}
		if (exception.getStatusCode() == UNAUTHORIZED) {
			return new RegoppslagSecurityException(format("Kall mot Regoppslag feilet. Ingen tilgang. problemDetail=%s", lesProblemDetail(exception)));
		}
		if (exception.getStatusCode() == NOT_FOUND) {
			return new UkjentAdresseException("Fant ikke adresseinformasjon for mottaker i PDL. Mottaker har ukjent adresse.");
		}
		if (exception.getStatusCode() == GONE) {
			return new PersonErDoedUkjentAdresseException("Mottaker er død og har ukjent adresse.");
		}
		return new RegoppslagFunctionalException(format("Henting av adresse for bruker feilet funksjonelt mot Regoppslag. status=%s, problemDetail=%s", exception.getStatusCode(), lesProblemDetail(exception)));
	}

	private ProblemDetail lesProblemDetail(RestClientResponseException exception) {
		try {
			return objectMapper.readValue(exception.getResponseBodyAsByteArray(), ProblemDetail.class);
		} catch (Exception e) {
			return ProblemDetail.forStatusAndDetail(exception.getStatusCode(), exception.getResponseBodyAsString());
		}
	}
}
