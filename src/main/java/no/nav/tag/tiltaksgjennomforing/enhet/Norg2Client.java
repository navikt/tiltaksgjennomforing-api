package no.nav.tag.tiltaksgjennomforing.enhet;

import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@Validated
public class Norg2Client {
    private final String baseGeografiskUrl;
    private final String baseOppfølgingUrl;
    private final RestTemplate restTemplate;

    public Norg2Client(
        Norg2GeografiskProperties norg2GeografiskProperties,
        Norg2OppfølgingProperties norg2OppfølgingProperties,
        RestTemplate noAuthRestTemplate
    ) {
        this.baseGeografiskUrl = norg2GeografiskProperties.getUrl();
        this.baseOppfølgingUrl = norg2OppfølgingProperties.getUrl();
        this.restTemplate = noAuthRestTemplate;
    }

    @Cacheable(CacheConfig.NORGNAVN_CACHE)
    public Norg2OppfølgingResponse hentOppfølgingsEnhetFraCacheNorg2(String enhet) {
        return this.hentOppfølgingsEnhet(enhet);
    }

    @Cacheable(CacheConfig.NORG_GEO_ENHET)
    public Norg2GeoResponse hentGeoEnhetFraCacheEllerNorg2(String geoTilknytning) {
        return this.hentGeografiskEnhet(geoTilknytning);
    }


    public Norg2GeoResponse hentGeografiskEnhet(String geoOmråde) {
        try {
            Norg2GeoResponse norg2GeoResponse = restTemplate.getForObject(
                baseGeografiskUrl + "{geoOmråde}",
                Norg2GeoResponse.class,
                Map.of("geoOmråde", geoOmråde)
            );
            if (norg2GeoResponse.getEnhetNr() == null) {
                log.warn("Fant ikke enhet med geoOmråde {}", geoOmråde);
            }
            return norg2GeoResponse;
        } catch (Exception e) {
            log.error("Feil v/oppslag på geoOmråde {}", geoOmråde);
            throw e;
        }
    }

    public Norg2OppfølgingResponse hentOppfølgingsEnhet(@Pattern(regexp = "^\\d{4}$", message = "Ugyldig enhetsnummer") String enhetsnummer) {
        try {
            Norg2OppfølgingResponse norg2OppfølgingResponse = restTemplate.getForObject(
                baseOppfølgingUrl + "{enhetsnummer}",
                Norg2OppfølgingResponse.class,
                Map.of("enhetsnummer", enhetsnummer)
            );
            if (Objects.requireNonNull(norg2OppfølgingResponse).getNavn() == null) {
                log.warn("Fant ingen enhet: {}", enhetsnummer);
            }
            return norg2OppfølgingResponse;
        } catch (Exception e) {
            log.warn("Feil v/oppslag på enhet {}", enhetsnummer, e);
            return null;
        }
    }
}
