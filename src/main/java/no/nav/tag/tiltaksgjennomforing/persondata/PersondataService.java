package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.sts.STSClient;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
        if ("FORTROLIG".equals(gradering) || "STRENGT_FORTROLIG".equals(gradering)) {
            throw new TilgangskontrollException("Du har ikke tilgang til deltaker");
        }
    }

    private HttpEntity<String> createRequestEntity(Fnr fnr) {
        String stsToken = stsClient.hentSTSToken().getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(stsToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Tema", "GEN");
        headers.set("Nav-Consumer-Token", "Bearer " + stsToken);
        return new HttpEntity<>(createQuery(fnr), headers);
    }

    private String createQuery(Fnr fnr) {
        return String.format("{\"query\" : \"query{ hentPerson( ident: \\\"%s\\\") {adressebeskyttelse {gradering} } }\"}", fnr.asString());
    }

    private Adressebeskyttelse hentGraderingFraPdlRespons(ResponseEntity<PdlPerson> response) {
        if (response.getBody().getData().getHentPerson() == null) {
            // Person finnes ikke
            return new Adressebeskyttelse("");
        } else {
            return response.getBody().getData().getHentPerson().getAdressebeskyttelse()[0];
        }
    }

    private Adressebeskyttelse getFraPdl(Fnr fnr) {
        try {
            ResponseEntity<PdlPerson> response = restTemplate.exchange(persondataProperties.getUri(), HttpMethod.POST, createRequestEntity(fnr), PdlPerson.class);
            return hentGraderingFraPdlRespons(response);
        } catch (RestClientException exception) {
            stsClient.evictToken();
            log.error("Feil fra PDL med request-url: " + persondataProperties.getUri(), exception);
            throw exception;
        }
    }
}