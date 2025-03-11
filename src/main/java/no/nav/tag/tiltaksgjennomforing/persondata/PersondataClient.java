package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.CacheConfig;
import no.nav.tag.tiltaksgjennomforing.persondata.domene.PdlRequest;
import no.nav.tag.tiltaksgjennomforing.persondata.domene.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.domene.PdlResponsBolk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PersondataClient {
    private static final String BEHANDLINGSNUMMER = "B662";

    private final RestTemplate azureRestTemplate;
    private final PersondataProperties persondataProperties;

    @Value("classpath:pdl/hentPersonBolk.graphql")
    private Resource adressebeskyttelseBolkQueryResource;

    @Value("classpath:pdl/hentPersondata.graphql")
    private Resource persondataQueryResource;

    public PersondataClient(
        RestTemplate azureRestTemplate,
        PersondataProperties persondataProperties
    ) {
        this.azureRestTemplate = azureRestTemplate;
        this.persondataProperties = persondataProperties;
    }

    public PdlResponsBolk hentPersonBolk(Set<Fnr> fnr) {
        try {
            PdlRequest<PdlRequest.BolkVariables> pdlRequest = new PdlRequest<>(
                adressebeskyttelseBolkQueryResource,
                new PdlRequest.BolkVariables(fnr.stream().map(Fnr::asString).collect(Collectors.toList()))
            );
            return azureRestTemplate.postForObject(persondataProperties.getUri(), createRequestEntity(pdlRequest), PdlResponsBolk.class);
        } catch (RestClientException exception) {
            log.error("Feil fra PDL med request-url: {}", persondataProperties.getUri(), exception);
            throw exception;
        }
    }

    @Cacheable(CacheConfig.PDL_CACHE)
    public PdlRespons hentPersondata(Fnr fnr) {
        try {
            PdlRequest<PdlRequest.Varaibles> pdlRequest = new PdlRequest<>(
                persondataQueryResource,
                new PdlRequest.Varaibles(fnr.asString())
            );
            return azureRestTemplate.postForObject(persondataProperties.getUri(), createRequestEntity(pdlRequest), PdlRespons.class);
        } catch (RestClientException exception) {
            log.error("Feil fra PDL med request-url: {}", persondataProperties.getUri(), exception);
            throw exception;
        }
    }

    private HttpEntity<String> createRequestEntity(PdlRequest<?> pdlRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Behandlingsnummer", BEHANDLINGSNUMMER);
        return new HttpEntity(pdlRequest, headers);
    }
}
