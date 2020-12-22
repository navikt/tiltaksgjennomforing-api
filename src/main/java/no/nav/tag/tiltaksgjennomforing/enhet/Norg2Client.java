package no.nav.tag.tiltaksgjennomforing.enhet;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@AllArgsConstructor
public class Norg2Client {

    private final Norg2Properties norg2Properties;
    private final RestTemplate restTemplate;

    @SneakyThrows
    public String hentGeografiskEnhet(String geoOmråde) {
        return restTemplate.getForObject(norg2Properties.getUrl() + geoOmråde, Norg2Response.class).getEnhetId();
    }
}

