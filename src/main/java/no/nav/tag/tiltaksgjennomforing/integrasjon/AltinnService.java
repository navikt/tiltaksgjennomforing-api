package no.nav.tag.tiltaksgjennomforing.integrasjon;

import no.nav.tag.tiltaksgjennomforing.controller.TokenUtils;
import no.nav.tag.tiltaksgjennomforing.domene.Identifikator;
import no.nav.tag.tiltaksgjennomforing.domene.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.AltinnException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AltinnService {

    private final TokenUtils tokenUtils;
    private final AltinnConfiguration altinnConfiguration;

    @Autowired
    public AltinnService(TokenUtils tokenUtils, AltinnConfiguration altinnConfiguration) {
        this.tokenUtils = tokenUtils;
        this.altinnConfiguration = altinnConfiguration;
    }


    public  List<Organisasjon> hentOrganisasjoner(Identifikator fnr) {
        String query = "&subject=" + fnr;
        ResponseEntity<List<AltinnOrganisasjon>> respons = getFromAltinn(new ParameterizedTypeReference<List<AltinnOrganisasjon>>() {},query);
        return respons.getBody().stream()
                .map(AltinnOrganisasjon::create)
                .collect(Collectors.toList());
    }

    private  HttpEntity<String>  getheaderEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NAV-APIKEY", altinnConfiguration.getApikey());
        headers.set("APIKEY", altinnConfiguration.getGw_apikey());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return entity;
    }


    private  <T> ResponseEntity<List<T>> getFromAltinn(ParameterizedTypeReference<List<T>> typeReference, String query)  {
        String url = altinnConfiguration.getAltinn_url() + "/reportees/?ForceEIAuthentication" + query;
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> headers = getheaderEntity();

        try {
            ResponseEntity<List<T>> respons = restTemplate.exchange(url,
                    HttpMethod.GET, headers, typeReference);
            return respons;

        } catch (RestClientException exception) {
           // log.error("Feil fra Altinn. Exception: ", exception);
            throw new AltinnException("Feil fra Altinn", exception);
        }

    }






}


