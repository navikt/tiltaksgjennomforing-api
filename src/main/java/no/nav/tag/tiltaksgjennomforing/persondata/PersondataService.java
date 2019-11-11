package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.sts.STSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import scala.Console;

import java.util.logging.ConsoleHandler;

@Slf4j
@Service
public class PersondataService {
    private final RestTemplate restTemplate;
    private final STSClient stsClient;
    @Value("${tiltaksgjennomforing.persondata.uri}")
    private String pdlUrl;

    public PersondataService(RestTemplate restTemplate, STSClient stsClient) {
        this.restTemplate = restTemplate;
        this.stsClient = stsClient;
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
        headers.set("Authorization", "Bearer " + stsClient.hentSTSToken().getAccessToken());
        headers.set("Tema", "GEN");
        headers.set("Nav-Consumer-Token", stsClient.hentSTSToken().getAccessToken());
        headers.set("Content-Type", "application/json");
        return new HttpEntity<>(createQuery(fnr),headers);
    }

    private String createQuery(Fnr fnr) {
        return String.format("{\"query\" : \"query{ hentPerson( ident: \\\"%s\\\") {adressebeskyttelse {gradering} } }\"}", fnr.asString());
    }

    private Adressebeskyttelse getFraPdl(Fnr fnr){
        try {
            ResponseEntity<PdlPerson> result = restTemplate.exchange(pdlUrl, HttpMethod.POST, createRequestEntity(fnr), PdlPerson.class);
            if (result.getStatusCode()!= HttpStatus.OK){
                String message = "Kall mot pdl feiler med HTTP-" + result.getStatusCode();
                log.error(message);
                throw new RuntimeException(message);
            }
            return result.getBody().getData().getHentPerson().getAdressebeskyttelse()[0];
        } catch (RestClientException exception) {
            log.error("Feil fra PDL med spørring: " + pdlUrl + " Exception: " + exception.getMessage());
            throw new TiltaksgjennomforingException("Feil fra PDL", exception);
        }
    }
}
