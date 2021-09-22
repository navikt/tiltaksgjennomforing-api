package no.nav.tag.tiltaksgjennomforing.enhet;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class Norg2Client {

    private final Norg2GeografiskProperties norg2GeografiskProperties;
    private final Norg2OppfølgingProperties norg2OppfølgingProperties;
    private final RestTemplate restTemplate;

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

    public Norg2OppfølgingResponse hentOppfølgingsEnhetsnavn(String enhet) {
        Norg2OppfølgingResponse norg2OppfølgingResponse = null;
        try {
            norg2OppfølgingResponse = restTemplate.getForObject(norg2OppfølgingProperties.getUrl() + enhet, Norg2OppfølgingResponse.class);
            if (Objects.requireNonNull(norg2OppfølgingResponse).getNavn() == null) {
                log.warn("Fant ingen navn til enhet: {}", enhet);
            }
        }catch (Exception e) {
            log.error("Feil v/oppslag på enhet {}", enhet);
        }
        return norg2OppfølgingResponse;
    }
}

