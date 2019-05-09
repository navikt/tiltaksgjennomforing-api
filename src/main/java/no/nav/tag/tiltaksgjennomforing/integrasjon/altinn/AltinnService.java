package no.nav.tag.tiltaksgjennomforing.integrasjon.altinn;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.domene.Identifikator;
import no.nav.tag.tiltaksgjennomforing.domene.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.AltinnProperties;
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
public class AltinnService {
    private final AltinnProperties altinnProperties;
    private final RestTemplate restTemplate;

    public AltinnService(AltinnProperties altinnProperties) {
        this.altinnProperties = altinnProperties;
        restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList((request, body, execution) -> {
            request.getHeaders().add("X-NAV-APIKEY", altinnProperties.getApiGwApiKey());
            request.getHeaders().add("APIKEY", altinnProperties.getAltinnApiKey());
            return execution.execute(request, body);
        }));
    }

    private static List<Organisasjon> konverterTilDomeneObjekter(AltinnOrganisasjon[] altinnOrganisasjoner) {
        return Arrays.stream(altinnOrganisasjoner)
                .map(AltinnOrganisasjon::konverterTilDomeneObjekt)
                .collect(Collectors.toList());
    }

    public List<Organisasjon> hentOrganisasjoner(Identifikator fnr) {
        URI uri = UriComponentsBuilder.fromUri(altinnProperties.getAltinnUri())
                .pathSegment("ekstern", "altinn", "api", "serviceowner", "reportees")
                .queryParam("ForceEIAuthentication")
                .queryParam("subject", fnr.asString())
                .queryParam("serviceCode", altinnProperties.getServiceCode())
                .queryParam("serviceEdition", altinnProperties.getServiceEdition())
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