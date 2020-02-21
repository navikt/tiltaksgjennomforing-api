package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Arbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.orgenhet.ArbeidsgiverOrganisasjon;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
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

    private static List<ArbeidsgiverOrganisasjon> konverterTilDomeneObjekter(AltinnOrganisasjon[] altinnOrganisasjoner, Tiltakstype tilgangstype) {
        return Arrays.stream(altinnOrganisasjoner)
                .map(AltinnOrganisasjon -> AltinnOrganisasjon.konverterTilDomeneObjekt(tilgangstype))
                .collect(Collectors.toList());
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
        List<ArbeidsgiverOrganisasjon> organisasjoner = new ArrayList<>();
        organisasjoner.addAll(kallAltinn(altinnTilgangsstyringProperties.getArbtreningServiceCode(), altinnTilgangsstyringProperties.getArbtreningServiceEdition(), fnr, Tiltakstype.ARBEIDSTRENING));
        organisasjoner.addAll(kallAltinn(altinnTilgangsstyringProperties.getLtsMidlertidigServiceCode(), altinnTilgangsstyringProperties.getLtsMidlertidigServiceEdition(), fnr, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD));
        organisasjoner.addAll(kallAltinn(altinnTilgangsstyringProperties.getLtsVarigServiceCode(), altinnTilgangsstyringProperties.getLtsVarigServiceEdition(), fnr, Tiltakstype.VARIG_LONNSTILSKUDD));
        return organisasjoner;
    }

    private List<ArbeidsgiverOrganisasjon> kallAltinn(Integer serviceCode, Integer serviceEdition, Identifikator fnr, Tiltakstype tilgangstype) {
        try {
            AltinnOrganisasjon[] altinnOrganisasjoner = restTemplate.getForObject(lagAltinnUrl(serviceCode, serviceEdition, fnr), AltinnOrganisasjon[].class);
            return konverterTilDomeneObjekter(altinnOrganisasjoner, tilgangstype);
        } catch (RestClientException exception) {
            log.warn("Feil ved kall mot Altinn.", exception);
            throw new TiltaksgjennomforingException("Det har skjedd en feil ved oppslag mot Altinn. Forsøk å laste siden på nytt");
        }
    }
}