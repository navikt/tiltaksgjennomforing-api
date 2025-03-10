package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.CacheConfig;
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

    public PdlRespons hentPersonBolk(Set<Fnr> fnr) {
        PdlRequest<PdlRequestVariables.IdentBolk> pdlRequest = new PdlRequest<>(
            adressebeskyttelseBolkQueryResource,
            new PdlRequestVariables.IdentBolk(fnr.stream().map(Fnr::asString).collect(Collectors.toList()))
        );
        return utførKallTilPdl(pdlRequest);
    }

    @Cacheable(CacheConfig.PDL_CACHE)
    public PdlRespons hentPersondata(Fnr fnr) {
        PdlRequest<PdlRequestVariables.Ident> pdlRequest = new PdlRequest<>(
            persondataQueryResource,
            new PdlRequestVariables.Ident(fnr.asString())
        );
        return utførKallTilPdl(pdlRequest);
    }

    private PdlRespons utførKallTilPdl(PdlRequest<?> pdlRequest) {
        try {
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
