package no.nav.tag.tiltaksgjennomforing.okonomi.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KontoregisterClient {
/*
    private final String url;
    private final RestTemplate restTemplate;

    public KontoregisterClient(@Value("${tiltaksgjennomforing.kontoregister.uri}") String url, @Qualifier("azure") RestTemplate restTemplate) {
        this.url = url;
        this.restTemplate = restTemplate;
    }

    public String hentKontonummer() {
        return restTemplate.getForObject(url + "/990983666", String.class);
    }

    public String ping() {
        return restTemplate.getForObject(url, String.class);
    }
    */
}
