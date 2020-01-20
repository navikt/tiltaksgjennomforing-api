package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AltinnTilgangsstyringService {
    private final AltinnTilgangsstyringProperties altinnTilgangsstyringProperties;
    private final RestTemplate restTemplate;
    private static final int ALTINN_ORG_PAGE_SIZE = 500;

    public AltinnTilgangsstyringService(AltinnTilgangsstyringProperties altinnTilgangsstyringProperties) {
        this.altinnTilgangsstyringProperties = altinnTilgangsstyringProperties;
        restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList((request, body, execution) -> {
            request.getHeaders().add("X-NAV-APIKEY", altinnTilgangsstyringProperties.getApiGwApiKey());
            request.getHeaders().add("APIKEY", altinnTilgangsstyringProperties.getAltinnApiKey());
            return execution.execute(request, body);
        }));
    }

    private static List<Organisasjon> konverterTilDomeneObjekter(AltinnOrganisasjon[] altinnOrganisasjoner) {
        return Arrays.stream(altinnOrganisasjoner)
                .map(AltinnOrganisasjon::konverterTilDomeneObjekt)
                .collect(Collectors.toList());
    }

    public List<Organisasjon> hentOrganisasjoner(Identifikator fnr) {
        URI uri = UriComponentsBuilder.fromUri(altinnTilgangsstyringProperties.getUri())
                .queryParam("ForceEIAuthentication")
                .queryParam("subject", fnr.asString())
                .queryParam("serviceCode", altinnTilgangsstyringProperties.getServiceCode())
                .queryParam("serviceEdition", altinnTilgangsstyringProperties.getServiceEdition())
                .queryParam("$filter", "Type+ne+'Person'")
                .queryParam("$top", ALTINN_ORG_PAGE_SIZE)
                .build()
                .toUri();
        try {
            AltinnOrganisasjon[] altinnOrganisasjoner = restTemplate.getForObject(uri, AltinnOrganisasjon[].class);
            return konverterTilDomeneObjekter(altinnOrganisasjoner);
        } catch (RestClientException exception) {
            log.warn("Feil ved kall mot Altinn. Bedrifter som innlogget bruker kan representere settes til tom liste.", exception);
            return Collections.emptyList();
        }
    }
}