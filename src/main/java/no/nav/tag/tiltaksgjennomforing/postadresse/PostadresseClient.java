package no.nav.tag.tiltaksgjennomforing.postadresse;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

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
public class PostadresseClient {
	private final RestTemplate azureRestTemplate;
	private final String baseUrlForRegoppslagAPI;
	private final ObjectMapper objectMapper;

	public PostadresseClient(
		RestTemplate azureRestTemplate,
		RegoppslagProperties regoppslagProperties,
		ObjectMapper objectMapper
	) {
		this.azureRestTemplate = azureRestTemplate;
		this.baseUrlForRegoppslagAPI = regoppslagProperties.getUri().toString();
		this.objectMapper = objectMapper;
	}

	public boolean sjekkOmPersonErRegistrertMedRiktigAdresse(Fnr fnr) {
		return hentPostadresseHvisTilgjengelig(fnr).isPresent();
	}

	private Optional<PostadresseResponse> hentPostadresseHvisTilgjengelig(Fnr fnr) {
		try {
			PostadresseResponse response = hentPostadresse(fnr);
			if (response == null || response.adresse() == null) {
				log.warn("Fant ikke respons/adresse fra Regoppslag. Returnerer Optional.empty().");
				return Optional.empty();
			}
			return Optional.of(response);
		} catch (RegoppslagFunctionalException | RegoppslagTechnicalException exception) {
			log.warn("Fant ikke gyldig adresse fra Regoppslag. Returnerer Optional.empty(). feiltype={}", exception.getClass().getSimpleName());
			return Optional.empty();
		}
	}

	PostadresseResponse hentPostadresse(Fnr fnr) {
		try {
			return azureRestTemplate.postForObject(
				baseUrlForRegoppslagAPI + "/postadresse",
				lagRequest(fnr),
				PostadresseResponse.class
			);
		} catch (RestClientResponseException exception) {
			if (exception.getStatusCode().is5xxServerError()) {
				throw new RegoppslagTechnicalException(format("Kall mot Regoppslag feilet teknisk. status=%s, problemDetail=%s", exception.getStatusCode(), lesProblemDetail(exception)));
			}
			if (exception.getStatusCode() == UNAUTHORIZED) {
				throw new RegoppslagSecurityException(format("Kall mot Regoppslag feilet. Ingen tilgang. problemDetail=%s", lesProblemDetail(exception)));
			}
			if (exception.getStatusCode() == NOT_FOUND) {
				throw new UkjentAdresseException("Fant ikke adresseinformasjon for mottaker i PDL. Mottaker har ukjent adresse.");
			}
			if (exception.getStatusCode() == GONE) {
				throw new PersonErDoedUkjentAdresseException("Mottaker er død og har ukjent adresse.");
			}
			throw new RegoppslagFunctionalException(format("Henting av adresse for bruker feilet funksjonelt mot Regoppslag. status=%s, problemDetail=%s", exception.getStatusCode(), lesProblemDetail(exception)));
		} catch (RestClientException exception) {
			throw new RegoppslagTechnicalException(format("Kall mot Regoppslag feilet teknisk. feil=%s", exception.getMessage()), exception);
		}
	}

	private HttpEntity<PostadresseRequest> lagRequest(Fnr fnr) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		//headers.set("behandlingsnummer", "B662");
		return new HttpEntity<>(PostadresseRequest.builder()
			.ident(fnr.asString())
			.build(), headers);
	}

	private ProblemDetail lesProblemDetail(RestClientResponseException exception) {
		try {
			return objectMapper.readValue(exception.getResponseBodyAsByteArray(), ProblemDetail.class);
		} catch (Exception e) {
			return ProblemDetail.forStatusAndDetail(exception.getStatusCode(), exception.getResponseBodyAsString());
		}
	}
}
