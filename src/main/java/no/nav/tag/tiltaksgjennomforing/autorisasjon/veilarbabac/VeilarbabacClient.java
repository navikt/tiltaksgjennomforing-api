package no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac;

import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.restservicecache.CacheConfiguration;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.sts.STSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class VeilarbabacClient {
    
	private final RestTemplate restTemplate;
    private final STSClient stsClient;
    private final String veilarbabacUrl;

    static final String PERMIT_RESPONSE = "permit";
    static final String DENY_RESPONSE = "deny";

    public VeilarbabacClient(
            RestTemplate restTemplate,
            STSClient stsClient,
            @Value("${tiltaksgjennomforing.abac.veilarbabac-uri}") String veilarbabacUrl
    ) {
        this.restTemplate = restTemplate;
        this.stsClient = stsClient;
        this.veilarbabacUrl = veilarbabacUrl;
    }

    @Cacheable(CacheConfiguration.ABAC_CACHE)
    public boolean sjekkTilgang(String fnr, TilgangskontrollAction action, NavIdent navIdent) {
        String response;
        try {
            response = hentTilgang(fnr, action, navIdent);
        } catch (HttpClientErrorException e) {
            stsClient.evictToken();
            response = hentTilgang(fnr, action, navIdent);
        }

        if (PERMIT_RESPONSE.equals(response)) {
            return true;
        } else if (DENY_RESPONSE.equals(response)) {
            return false;
        }

        throw new TilgangskontrollException("Ukjent respons fra veilarbabac: " + response);
    }

    private String hentTilgang(String fnr, TilgangskontrollAction action, NavIdent identifikator) {
        String uriString = UriComponentsBuilder.fromHttpUrl(veilarbabacUrl)
                .path("/person")
                .queryParam("fnr", fnr)
                .queryParam("action", action.toString())
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("subject", identifikator.asString());
        headers.set("subjectType", "InternBruker");
        headers.setBearerAuth(hentOidcTokenTilSystembruker());
        headers.setContentType(MediaType.APPLICATION_JSON);

        return restTemplate.exchange(
                uriString,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        ).getBody();
    }

    private String hentOidcTokenTilSystembruker() {
        return stsClient.hentSTSToken().getAccessToken();
    }
    
    @CacheEvict(cacheNames=CacheConfiguration.ABAC_CACHE, allEntries=true)
    public void cacheEvict() {
    }

}
