package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.AltinnFeilException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;
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
    private static final int ALTINN_ORG_PAGE_SIZE = 500;
    private final AltinnTilgangsstyringProperties altinnTilgangsstyringProperties;
    private final RestTemplate restTemplate;
    private final TokenUtils tokenUtils;
    private final FeatureToggleService featureToggleService;

    public AltinnTilgangsstyringService(
            AltinnTilgangsstyringProperties altinnTilgangsstyringProperties,
            TokenUtils tokenUtils,
            FeatureToggleService featureToggleService
    ) {
        if (Utils.erNoenTomme(altinnTilgangsstyringProperties.getArbtreningServiceCode(),
                altinnTilgangsstyringProperties.getArbtreningServiceEdition(),
                altinnTilgangsstyringProperties.getLtsMidlertidigServiceCode(),
                altinnTilgangsstyringProperties.getLtsMidlertidigServiceEdition(),
                altinnTilgangsstyringProperties.getLtsVarigServiceCode(),
                altinnTilgangsstyringProperties.getLtsVarigServiceEdition())) {
            throw new TiltaksgjennomforingException("Altinn konfigurasjon ikke komplett");
        }
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

    public Map<BedriftNr, Collection<Tiltakstype>> hentTilganger(Fnr fnr) {
        Multimap<BedriftNr, Tiltakstype> tilganger = HashMultimap.create();

        AltinnOrganisasjon[] arbeidstreningOrger = kallAltinn(altinnTilgangsstyringProperties.getArbtreningServiceCode(), altinnTilgangsstyringProperties.getArbtreningServiceEdition(), fnr);
        leggTil(tilganger, arbeidstreningOrger, Tiltakstype.ARBEIDSTRENING);

        AltinnOrganisasjon[] varigLtsOrger = kallAltinn(altinnTilgangsstyringProperties.getLtsVarigServiceCode(), altinnTilgangsstyringProperties.getLtsVarigServiceEdition(), fnr);
        leggTil(tilganger, varigLtsOrger, Tiltakstype.VARIG_LONNSTILSKUDD);

        AltinnOrganisasjon[] midlLtsOrger = kallAltinn(altinnTilgangsstyringProperties.getLtsMidlertidigServiceCode(), altinnTilgangsstyringProperties.getLtsMidlertidigServiceEdition(), fnr);
        leggTil(tilganger, midlLtsOrger, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);

        return tilganger.asMap();
    }

    private void leggTil(Multimap<BedriftNr, Tiltakstype> tilganger, AltinnOrganisasjon[] arbeidstreningOrger, Tiltakstype tiltakstype) {
        for (AltinnOrganisasjon altinnOrganisasjon : arbeidstreningOrger) {
            if (!altinnOrganisasjon.getType().equals("Enterprise")) {
                tilganger.put(new BedriftNr(altinnOrganisasjon.getOrganizationNumber()), tiltakstype);
            }
        }
    }

    public Set<AltinnOrganisasjon> hentAltinnOrganisasjoner(Fnr fnr) {
        return new HashSet<>(List.of(kallAltinn(null, null, fnr)));
    }

    private AltinnOrganisasjon[] kallAltinn(Integer serviceCode, Integer serviceEdition, Fnr fnr) {
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
            throw new AltinnFeilException();
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
