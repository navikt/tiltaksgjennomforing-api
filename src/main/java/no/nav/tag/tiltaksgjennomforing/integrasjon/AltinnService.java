package no.nav.tag.tiltaksgjennomforing.integrasjon;

import no.nav.tag.tiltaksgjennomforing.domene.Identifikator;
import no.nav.tag.tiltaksgjennomforing.domene.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.AltinnException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.AltinnProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AltinnService {
    private final AltinnProperties altinnProperties;
    private final RestTemplate restTemplate;

    public AltinnService(AltinnProperties altinnProperties) {
        this.altinnProperties = altinnProperties;
        restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Arrays.asList((request, body, execution) -> {
            request.getHeaders().add("X-NAV-APIKEY", altinnProperties.getAltinnApiKey());
            request.getHeaders().add("APIKEY", altinnProperties.getApiGwApiKey());
            return execution.execute(request, body);
        }));
    }

    private static List<Organisasjon> konverterTilDomeneObjekter(List<AltinnOrganisasjon> altinnOrganisasjoner) {
        return altinnOrganisasjoner.stream()
                .map(AltinnOrganisasjon::konverterTilDomeneObjekt)
                .collect(Collectors.toList());
    }

    public List<Organisasjon> hentOrganisasjoner(Identifikator fnr) {
        String uri = UriComponentsBuilder.fromUri(altinnProperties.getAltinnUrl())
                .pathSegment("reportees")
                .queryParam("ForceEIAuthentication", true)
                .queryParam("subject", fnr.asString())
                .toUriString();
        try {
            List<AltinnOrganisasjon> altinnOrganisasjoner = restTemplate.getForObject(uri, List.class);
            return konverterTilDomeneObjekter(altinnOrganisasjoner);
        } catch (RestClientException exception) {
            throw new AltinnException("Feil fra Altinn", exception);
        }
    }
}


