package no.nav.tag.tiltaksgjennomforing.integrasjon;

import no.nav.tag.tiltaksgjennomforing.controller.TokenUtils;
import no.nav.tag.tiltaksgjennomforing.domene.Organisasjon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@Component
public class OrganisasjonerService {

    private final TokenUtils tokenUtils;

    @Autowired
    public OrganisasjonerService(TokenUtils tokenUtils) {
        this.tokenUtils = tokenUtils;
    }


    public  List<Organisasjon> getOrganisasjoner() throws IOException {
        String dnaGetOrganisasjoner = "https://arbeidsgiver-q.nav.no/ditt-nav-arbeidsgiver/api/organisasjoner";

        ResponseEntity<List<Organisasjon>> respons = getFromDna(new ParameterizedTypeReference<List<Organisasjon>>() {},dnaGetOrganisasjoner);
        return respons.getBody();
    }



    private  <T> ResponseEntity<List<T>> getFromDna(ParameterizedTypeReference<List<T>> typeReference, String query)  {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> headers = getheaderEntity();
        try {
            ResponseEntity<List<T>> respons = restTemplate.exchange(query,
                    HttpMethod.GET, headers, typeReference);
            return respons;

        } catch (RestClientException exception) {
            System.out.println("Feil fra DNA lol");
            System.out.println(exception);
            return null;
        }
    }

    private  HttpEntity<String>  getheaderEntity() {
        try {
            String token = tokenUtils.hentToken();
            System.out.println("TOKEN: ");
            System.out.println(token);
        } catch (Exception exception) {
            System.out.println("Excpetion i tokenhenting: ");
            System.out.println(exception);
        }

        String idtoken = "";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "selvbetjening-idtoken=" + idtoken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return entity;
    }





}


