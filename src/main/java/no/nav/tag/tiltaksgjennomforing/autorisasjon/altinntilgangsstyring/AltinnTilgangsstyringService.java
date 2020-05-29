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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlientConfig;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.ProxyConfig;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.Subject;
import no.nav.security.oidc.context.TokenContext;

@Component
@Slf4j
public class AltinnTilgangsstyringService {
    private final AltinnTilgangsstyringProperties altinnTilgangsstyringProperties;
    private final RestTemplate restTemplate;
    private final TokenUtils tokenUtils;
    private final FeatureToggleService featureToggleService;
    private static final int ALTINN_ORG_PAGE_SIZE = 500;

    private final String proxyUrl;
    private final String altinnProxyFallbackUrl;
    private final AltinnrettigheterProxyKlient klient;

    public AltinnTilgangsstyringService(
            AltinnTilgangsstyringProperties altinnTilgangsstyringProperties,
            TokenUtils tokenUtils,
            FeatureToggleService featureToggleService
    ) {
        this.altinnTilgangsstyringProperties = altinnTilgangsstyringProperties;
        this.tokenUtils = tokenUtils;
        this.featureToggleService = featureToggleService;
        restTemplate = new RestTemplate();
        altinnProxyFallbackUrl = altinnTilgangsstyringProperties.getProxyFallbackUrl();
        proxyUrl = altinnTilgangsstyringProperties.getProxyUrl();

        AltinnrettigheterProxyKlientConfig proxyKlientConfig = new AltinnrettigheterProxyKlientConfig(
                new ProxyConfig(
                        "tiltaksgjennomforing-api",
                        proxyUrl
                ),
                new no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnConfig(
                        altinnProxyFallbackUrl,
                        altinnTilgangsstyringProperties.getAltinnHeader(),
                        altinnTilgangsstyringProperties.getAPIGwHeader()
                )
        );
        this.klient = new AltinnrettigheterProxyKlient(proxyKlientConfig);
    }

    private URI lagAltinnUrl(Integer serviceCode, Integer serviceEdition, Identifikator fnr) {
        return UriComponentsBuilder.fromHttpUrl(altinnTilgangsstyringProperties.getProxyFallbackUrl() + "/ekstern/altinn/api/serviceowner/reportees")
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

        boolean brukProxy = featureToggleService.isEnabled("arbeidsgiver.tiltaksgjennomforing-api.bruk-altinn-proxy");
        HttpEntity<HttpHeaders> headers = brukProxy ? getAuthHeadersForInnloggetBruker() : getAuthHeadersForAltinn();
        if (brukProxy) {
            Map<String, String> parametre = new ConcurrentHashMap<>();
            parametre.put("serviceCode", serviceCode.toString());
            parametre.put("serviceEdition", serviceEdition.toString());
            log.info("Henter rettigheter fra Altinn via proxy");
            List<AltinnOrganisasjon> reporteesFromAltinnViaProxy = getReporteesFromAltinnViaProxy(
                    tokenUtils.hentSelvbetjeningTokenContext(),
                    new Subject(fnr.asString()),
                    parametre,
                    ALTINN_ORG_PAGE_SIZE
            );
            return reporteesFromAltinnViaProxy.toArray(new AltinnOrganisasjon[0]);
        } else {
            try {
                return restTemplate.exchange(
                        lagAltinnUrl(serviceCode, serviceEdition, fnr),
                        HttpMethod.GET,
                        headers,
                        AltinnOrganisasjon[].class
                ).getBody();

            } catch (RestClientException exception) {
                log.warn("Feil ved kall mot Altinn.", exception);
                throw new TiltaksgjennomforingException("Det har skjedd en feil ved oppslag mot Altinn. Forsøk å laste siden på nytt");
            }
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

    List<AltinnOrganisasjon> getReporteesFromAltinnViaProxy(
            TokenContext tokenContext,
            Subject subject,
            Map<String, String> parametre,
            int pageSize
    ) {
        Set<AltinnOrganisasjon> response = new HashSet<>();
        int pageNumber = 0;
        boolean hasMore = true;
        while (hasMore) {
            pageNumber++;
            try {
                parametre.put("$top", String.valueOf(pageSize));
                parametre.put("$skip", String.valueOf(((pageNumber - 1) * pageSize)));
                List<AltinnOrganisasjon> collection = mapTo(
                        klient.hentOrganisasjoner(
                                tokenContext,
                                subject,
                                parametre
                        )
                );
                response.addAll(collection);
                hasMore = collection.size() >= pageSize;
            } catch (RestClientException exception) {
                log.error("Feil fra Altinn-proxy med spørring til Reportees, Exception: " + exception.getMessage());
                throw new TiltaksgjennomforingException("Feil fra Altinn", exception);
            }
        }
        return new ArrayList<>(response);
    }

    private List<AltinnOrganisasjon> mapTo(List<AltinnReportee> altinnReportees) {
        return altinnReportees.stream().map(org -> {
                    AltinnOrganisasjon altinnOrganisasjon = new AltinnOrganisasjon();
                    altinnOrganisasjon.setName(org.getName());
                    altinnOrganisasjon.setType(org.getType());
                    altinnOrganisasjon.setOrganizationNumber(org.getOrganizationNumber());
                    altinnOrganisasjon.setOrganizationForm(org.getOrganizationForm());
                    altinnOrganisasjon.setStatus(org.getStatus());
                    return altinnOrganisasjon;
                }
        ).collect(Collectors.toList());
    }
}
