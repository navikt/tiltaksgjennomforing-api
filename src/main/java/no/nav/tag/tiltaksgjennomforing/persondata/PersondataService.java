package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.sts.STSClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.Null;

@Slf4j
@Service
public class PersondataService {
    private final RestTemplate restTemplate;
    private final STSClient stsClient;
    private final PersondataProperties persondataProperties;

    public PersondataService(RestTemplate restTemplate, STSClient stsClient, PersondataProperties persondataProperties) {
        this.restTemplate = restTemplate;
        this.stsClient = stsClient;
        this.persondataProperties = persondataProperties;
    }

    Adressebeskyttelse hentGradering(Fnr fnr) {
        return getFraPdl(fnr);
    }

    public void sjekkGradering(Fnr fnr) {
        String gradering = hentGradering(fnr).getGradering();
        if (gradering.equals("FORTROLIG") || gradering.equals("STRENGT_FORTROLIG")) {
            throw new TilgangskontrollException("Du har ikke tilgang til deltaker");
        }
    }

    private HttpEntity<String> createRequestEntity(Fnr fnr) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(stsClient.hentSTSToken().getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Tema", "GEN");
        headers.set("Nav-Consumer-Token", "Bearer " + stsClient.hentSTSToken().getAccessToken());
        return new HttpEntity<>(createQuery(fnr),headers);
    }

    private String createQuery(Fnr fnr) {
        return String.format("{\"query\" : \"query{ hentPerson( ident: \\\"%s\\\") {adressebeskyttelse {gradering} } }\"}", fnr.asString());
    }

    private Adressebeskyttelse hentGraderingFraPdlRespons(ResponseEntity<PdlPerson> response) {
        try{
            if (response.getBody().getData().getHentPerson() == null) {
                return new Adressebeskyttelse("");
            } else {
                return response.getBody().getData().getHentPerson().getAdressebeskyttelse()[0];
            }
        } catch (NullPointerException e) {
            log.error("nullpointer exception: {} ", e.getMessage());
            throw new TiltaksgjennomforingException("Feil fra PDL");
        }
    }

    private Adressebeskyttelse getFraPdl(Fnr fnr){
        try {
            ResponseEntity<PdlPerson> response = restTemplate.exchange(persondataProperties.getUri(), HttpMethod.POST, createRequestEntity(fnr), PdlPerson.class);
            if (response.getStatusCode()!= HttpStatus.OK){
                String message = "Kall mot pdl feiler med HTTP-" + response.getStatusCode();
                log.error(message);
                throw new RuntimeException(message);
            }

            return hentGraderingFraPdlRespons(response);

        } catch (RestClientException exception) {
            log.error("Feil fra PDL med spørring: " + persondataProperties.getUri() + " Exception: " + exception.getMessage());
            throw new TiltaksgjennomforingException("Feil fra PDL", exception);
        }
    }
}