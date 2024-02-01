package no.nav.tag.tiltaksgjennomforing.enhet;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.EhCacheConfig;
import org.apache.commons.compress.harmony.pack200.NewAttribute;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.Pattern;
import java.util.Objects;

@Slf4j
@Component
@AllArgsConstructor
@Validated
public class Norg2Client {

    private final Norg2GeografiskProperties norg2GeografiskProperties;
    private final Norg2OppfølgingProperties norg2OppfølgingProperties;
    private final RestTemplate restTemplate;

    @Cacheable(EhCacheConfig.NORGNAVN_CACHE)
    public Norg2OppfølgingResponse hentOppfølgingsEnhetsnavnFraCacheNorg2(String enhet) {
        return this.hentOppfølgingsEnhet(enhet);
    }

    @Cacheable(EhCacheConfig.NORG_GEO_ENHET)
    public Norg2GeoResponse hentGeoEnhetFraCacheEllerNorg2(String geoTilknytning) {
        return this.hentGeografiskEnhet(geoTilknytning);
    }


    public Norg2GeoResponse hentGeografiskEnhet(String geoOmråde) {
        Norg2GeoResponse norg2GeoResponse;
        try {
            norg2GeoResponse = restTemplate.getForObject(norg2GeografiskProperties.getUrl() + geoOmråde, Norg2GeoResponse.class);
            if (norg2GeoResponse.getEnhetNr() == null) {
                log.warn("Fant ikke enhet med geoOmråde {}", geoOmråde);
            }
        } catch (Exception e) {
            log.error("Feil v/oppslag på geoOmråde {}", geoOmråde);
            throw e;
        }
        return norg2GeoResponse;
    }

    public Norg2OppfølgingResponse hentOppfølgingsEnhet(@Pattern(regexp = "^\\d{4}$", message = "Ugyldig enhetsnummer") String enhetsnummer) {
        Norg2OppfølgingResponse norg2OppfølgingResponse = null;
        try {
            norg2OppfølgingResponse = restTemplate.getForObject(norg2OppfølgingProperties.getUrl() + enhetsnummer, Norg2OppfølgingResponse.class);
            if (Objects.requireNonNull(norg2OppfølgingResponse).getNavn() == null) {
                log.warn("Fant ingen enhet: {}", enhetsnummer);
            }
        } catch (Exception e) {
            log.error("Feil v/oppslag på enhet {}", enhetsnummer, e);
        }
        return norg2OppfølgingResponse;
    }
}
