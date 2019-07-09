package no.nav.tag.tiltaksgjennomforing.integrasjon.axsys;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.AxsysProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AxsysService {
    private final AxsysProperties axsysProperties;
    private final RestTemplate restTemplate;

    public AxsysService(AxsysProperties axsysProperties) {
        this.axsysProperties = axsysProperties;
        restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList((request, body, execution) -> {
            request.getHeaders().add("Authorization", "");
            request.getHeaders().add("Nav-Call-Id", axsysProperties.getNavCallId());
            request.getHeaders().add("Nav-Consumer-Id", axsysProperties.getNavConsumerId());
            return execution.execute(request, body);
        }));
    }


    private static List<NavIdent> konverterTilDomeneObjekter(AxsysIdent[] axsysIdenter) {
        return Arrays.stream(axsysIdenter)
                .map(AxsysIdent::konverterTilDomeneObjekt)
                .collect(Collectors.toList());
    }

    public List<NavIdent> hentIdenter(String enhetId) {
        URI uri = UriComponentsBuilder.fromUri(axsysProperties.getAxsysUri())
                .pathSegment("api", "v1", "enhet", enhetId, "brukere")
                .build()
                .toUri();

        try {
            AxsysIdent[] axsysIdenter = restTemplate.getForObject(uri, AxsysIdent[].class);
            return konverterTilDomeneObjekter(axsysIdenter);
        } catch (RestClientException exception) {
            throw new TiltaksgjennomforingException("Feil ved henting av identer for enhet " + enhetId, exception);
        }
    }
}
