package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import org.springframework.beans.factory.annotation.Value;
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
public class PersondataKlient {
    private static final String BEHANDLINGSNUMMER = "B662";

    private final RestTemplate azureRestTemplate;
    private final PersondataProperties persondataProperties;

    @Value("classpath:pdl/hentPerson.adressebeskyttelse.graphql")
    private Resource adressebeskyttelseQueryResource;

    @Value("classpath:pdl/hentPersonBolk.adressebeskyttelse.graphql")
    private Resource adressebeskyttelseBolkQueryResource;

    @Value("classpath:pdl/hentPersondata.graphql")
    private Resource persondataQueryResource;

    @Value("classpath:pdl/hentIdenter.graphql")
    private Resource identerQueryResource;

    public PersondataKlient(
        RestTemplate azureRestTemplate,
        PersondataProperties persondataProperties
    ) {
        this.azureRestTemplate = azureRestTemplate;
        this.persondataProperties = persondataProperties;
    }

    public PdlRespons hentAdressebeskyttelse(Fnr fnr) {
        PdlRequest<PdlRequestVariables.Ident> pdlRequest = new PdlRequest<>(
            adressebeskyttelseQueryResource,
            new PdlRequestVariables.Ident(fnr.asString())
        );
        return utførKallTilPdl(pdlRequest);
    }

    public PdlRespons hentAdressebeskyttelse(Set<Fnr> fnr) {
        PdlRequest<PdlRequestVariables.IdentBolk> pdlRequest = new PdlRequest<>(
            adressebeskyttelseBolkQueryResource,
            new PdlRequestVariables.IdentBolk(fnr.stream().map(Fnr::asString).collect(Collectors.toList()))
        );
        return utførKallTilPdl(pdlRequest);
    }

    public PdlRespons hentIdenter(Fnr fnr) {
        PdlRequest<PdlRequestVariables.Ident> pdlRequest = new PdlRequest<>(
            identerQueryResource,
            new PdlRequestVariables.Ident(fnr.asString())
        );
        return utførKallTilPdl(pdlRequest);
    }

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
