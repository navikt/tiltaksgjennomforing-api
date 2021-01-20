package no.nav.tag.tiltaksgjennomforing.enhet;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class Norg2Client {

    private final Norg2Properties norg2Properties;
    private final RestTemplate restTemplate;

    public String hentGeografiskEnhet(String geoOmråde) {
        Norg2Response norg2Response;
        try {
            norg2Response = restTemplate.getForObject(norg2Properties.getUrl() + geoOmråde, Norg2Response.class);
            if (norg2Response.getEnhetNr() == null) {
                log.warn("Fant ikke enhet med geoOmråde {}", geoOmråde);
            }
        } catch (Exception e) {
            log.error("Feil v/oppslag på geoOmråde {}", geoOmråde);
            throw e;
        }
        return Optional.of(norg2Response).map(Norg2Response::getEnhetNr).orElse(null);
    }
}

