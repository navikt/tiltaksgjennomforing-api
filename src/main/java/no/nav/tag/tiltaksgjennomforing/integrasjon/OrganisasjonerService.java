package no.nav.tag.tiltaksgjennomforing.integrasjon;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.tag.tiltaksgjennomforing.controller.TokenUtils;
import no.nav.tag.tiltaksgjennomforing.domene.Organisasjon;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Component
public class OrganisasjonerService {



    public static List<Organisasjon> getOrganisasjoner() throws IOException {
        TokenUtils tokenUtils = null;
        String query = "&subject=04010100653";
        String dnaGetOrganisasjoner = "https://arbeidsgiver-q.nav.no/ditt-nav-arbeidsgiver/api/organisasjoner" + query;

        ResponseEntity<List<Organisasjon>> respons = getFromDna(new ParameterizedTypeReference<List<Organisasjon>>() {},dnaGetOrganisasjoner);
        return respons.getBody();
    }



    private static <T> ResponseEntity<List<T>> getFromDna(ParameterizedTypeReference<List<T>> typeReference, String query)  {
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<T>> respons = restTemplate.exchange(query,
                    HttpMethod.GET, entity, typeReference);
            return respons;

        } catch (RestClientException exception) {
           // log.error("Feil fra Altinn. Exception: ", exception);
            //throw new Exception("Feil fra DNA", exception);
            System.out.println("Feil fra DNA lol");
            System.out.println(exception);
            return null;
        }
    }





}


