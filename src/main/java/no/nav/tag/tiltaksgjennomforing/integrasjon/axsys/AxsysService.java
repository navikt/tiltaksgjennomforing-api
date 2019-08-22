package no.nav.tag.tiltaksgjennomforing.integrasjon.axsys;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.domene.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.AxsysProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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
            request.getHeaders().add("Nav-Call-Id", UUID.randomUUID().toString());
            request.getHeaders().add("Nav-Consumer-Id", axsysProperties.getNavConsumerId());
            return execution.execute(request, body);
        }));
    }


    private static List<NavEnhet> konverterTilDomeneObjekter(AxsysRespons axsysEnheter) {
        return axsysEnheter.getEnheter().stream()
                .map(AxsysEnhet::konverterTilDomeneObjekt)
                .collect(Collectors.toList());
    }

    public List<NavEnhet> hentEnheterVeilederHarTilgangTil(NavIdent ident) {
        URI uri = UriComponentsBuilder.fromUri(axsysProperties.getAxsysUri())
                .pathSegment("api", "v1", "tilgang", ident.asString())
                .queryParam("inkluderAlleEnheter", "false")
                .build()
                .toUri();

        try {
            AxsysRespons axsysEnheter = restTemplate.getForObject(uri, AxsysRespons.class);
            return konverterTilDomeneObjekter(axsysEnheter);
        } catch (RestClientException exception) {
            log.warn("Feil ved henting av tilganger for ident " + ident, exception);
            throw new TiltaksgjennomforingException("Feil ved tilgangskontrollsjekk for ident " + ident.getId(), exception);
        }
    }
}