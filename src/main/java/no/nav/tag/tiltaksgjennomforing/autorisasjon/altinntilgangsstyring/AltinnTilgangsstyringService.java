package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.ArbeidsgiverOrganisasjon;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

@Component
@Slf4j
public class AltinnTilgangsstyringService {
    private final AltinnTilgangsstyringProperties altinnTilgangsstyringProperties;
    private final RestTemplate restTemplate;
    private final TokenUtils tokenUtils;
    private final FeatureToggleService featureToggleService;
    private static final int ALTINN_ORG_PAGE_SIZE = 500;

    public AltinnTilgangsstyringService(
            AltinnTilgangsstyringProperties altinnTilgangsstyringProperties,
            TokenUtils tokenUtils,
            FeatureToggleService featureToggleService
    ) {
        this.altinnTilgangsstyringProperties = altinnTilgangsstyringProperties;
        this.tokenUtils = tokenUtils;
        this.featureToggleService = featureToggleService;
        restTemplate = new RestTemplate();
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

    private URI lagAltinnProxyUrl(Integer serviceCode, Integer serviceEdition) {
        return UriComponentsBuilder.fromUri(altinnTilgangsstyringProperties.getProxyUri())
                .queryParam("ForceEIAuthentication")
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
            boolean brukProxy = featureToggleService.isEnabled("arbeidsgiver.tiltaksgjennomforing-api.bruk-altinn-proxy");
            HttpEntity<HttpHeaders> headers = brukProxy ? getAuthHeadersForInnloggetBruker() : getAuthHeadersForAltinn();
            URI uri = brukProxy ? lagAltinnProxyUrl(serviceCode, serviceEdition) : lagAltinnUrl(serviceCode, serviceEdition, fnr);
            return restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    headers,
                    AltinnOrganisasjon[].class
            ).getBody();
        } catch (RestClientException exception) {
            log.warn("Feil ved kall mot Altinn.", exception);
            throw new TiltaksgjennomforingException("Det har skjedd en feil ved oppslag mot Altinn. Forsøk å laste siden på nytt");
        }
    }

    private HttpEntity<HttpHeaders> getAuthHeadersForInnloggetBruker() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenUtils.hentSelvbetjeningToken());
        return new HttpEntity<>(headers);
    }

    private HttpEntity<HttpHeaders> getAuthHeadersForAltinn() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NAV-APIKEY", altinnTilgangsstyringProperties.getApiGwApiKey());
        headers.set("APIKEY", altinnTilgangsstyringProperties.getAltinnApiKey());
        return new HttpEntity<>(headers);
    }
}
