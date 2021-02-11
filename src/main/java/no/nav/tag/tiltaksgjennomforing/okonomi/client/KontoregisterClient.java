package no.nav.tag.tiltaksgjennomforing.okonomi.client;


import java.util.Objects;
import no.nav.tag.tiltaksgjennomforing.okonomi.KontoregisterResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KontoregisterClient {

    private final String url;
    private final RestTemplate restTemplate;

    public KontoregisterClient(@Value("${tiltaksgjennomforing.kontoregister.uri}") String url, @Qualifier("azure") RestTemplate restTemplate) {
        this.url = url;
        this.restTemplate = restTemplate;
    }

    public String hentKontonummer() {
        return Objects.requireNonNull(restTemplate.postForObject(url + "/990983666", lagRequest(), KontoregisterResponse.class)).getKontonr();
    }

    private HttpEntity lagRequest(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Nav-consumer-Id", "tiltaksgjennomforing-api");
        headers.set( "Nav-Call-Id","hoppla");
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity(headers);
    }


    public String ping() {
        return restTemplate.getForObject(url, String.class);
    }

}
