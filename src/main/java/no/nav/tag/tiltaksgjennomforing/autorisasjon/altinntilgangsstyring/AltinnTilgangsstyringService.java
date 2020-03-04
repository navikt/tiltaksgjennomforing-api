package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.orgenhet.ArbeidsgiverOrganisasjon;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
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

    private URI lagAltinnUrl(Integer serviceCode, Integer serviceEdition, Identifikator fnr) {
        return UriComponentsBuilder.fromUri(altinnTilgangsstyringProperties.getUri())
                .queryParam("ForceEIAuthentication")
                .queryParam("subject", fnr.asString())
                .queryParam("serviceCode", serviceCode)
                .queryParam("serviceEdition", serviceEdition)
                .queryParam("$filter", "Type+ne+'Person'")
                .queryParam("$top", ALTINN_ORG_PAGE_SIZE)
                .build()
                .toUri();
    }

    public List<ArbeidsgiverOrganisasjon> hentOrganisasjoner(Identifikator fnr) {
        Map<BedriftNr, ArbeidsgiverOrganisasjon> map = new HashMap<>();

        settInnIMap(map, Tiltakstype.ARBEIDSTRENING, kallAltinn(altinnTilgangsstyringProperties.getArbtreningServiceCode(), altinnTilgangsstyringProperties.getArbtreningServiceEdition(), fnr));
        settInnIMap(map, Tiltakstype.VARIG_LONNSTILSKUDD, kallAltinn(altinnTilgangsstyringProperties.getLtsVarigServiceCode(), altinnTilgangsstyringProperties.getLtsVarigServiceEdition(), fnr));
        settInnIMap(map, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, kallAltinn(altinnTilgangsstyringProperties.getLtsMidlertidigServiceCode(), altinnTilgangsstyringProperties.getLtsMidlertidigServiceEdition(), fnr));

        return new ArrayList<>(map.values());
    }

    private void settInnIMap(Map<BedriftNr, ArbeidsgiverOrganisasjon> map, Tiltakstype tiltakstype, AltinnOrganisasjon[] altinnOrganisasjons) {
        for (AltinnOrganisasjon altinnOrganisasjon : altinnOrganisasjons) {
            BedriftNr bedriftNr = new BedriftNr(altinnOrganisasjon.getOrganizationNumber());
            ArbeidsgiverOrganisasjon arbeidsgiverOrganisasjon;
            if (!map.containsKey(bedriftNr)) {
                arbeidsgiverOrganisasjon = new ArbeidsgiverOrganisasjon(bedriftNr, altinnOrganisasjon.getName());
                map.put(bedriftNr, arbeidsgiverOrganisasjon);
            } else {
                arbeidsgiverOrganisasjon = map.get(bedriftNr);
            }
            arbeidsgiverOrganisasjon.getTilgangstyper().add(tiltakstype);
        }
    }

    private AltinnOrganisasjon[] kallAltinn(Integer serviceCode, Integer serviceEdition, Identifikator fnr) {
        try {
            return restTemplate.getForObject(lagAltinnUrl(serviceCode, serviceEdition, fnr), AltinnOrganisasjon[].class);
        } catch (RestClientException exception) {
            log.warn("Feil ved kall mot Altinn.", exception);
            throw new TiltaksgjennomforingException("Det har skjedd en feil ved oppslag mot Altinn. Forsøk å laste siden på nytt");
        }
    }
}