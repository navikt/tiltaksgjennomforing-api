package no.nav.tag.tiltaksgjennomforing.autorisasjon.pilottilgang;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.CorrelationIdSupplier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class AxsysService {
    private final AxsysProperties axsysProperties;
    private final RestTemplate restTemplate;

    public AxsysService(AxsysProperties axsysProperties) {
        this.axsysProperties = axsysProperties;
        restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList((request, body, execution) -> {
            request.getHeaders().add("Nav-Call-Id", CorrelationIdSupplier.get());
            request.getHeaders().add("Nav-Consumer-Id", axsysProperties.getNavConsumerId());
            return execution.execute(request, body);
        }));
    }

    public List<NavEnhet> hentEnheterVeilederHarTilgangTil(NavIdent ident) {
        URI uri = UriComponentsBuilder.fromUri(axsysProperties.getUri())
                .pathSegment(ident.asString())
                .queryParam("inkluderAlleEnheter", "false")
                .build()
                .toUri();

        try {
            AxsysRespons respons = restTemplate.getForObject(uri, AxsysRespons.class);
            return respons.tilEnheter();
        } catch (RestClientException exception) {
            log.warn("Feil ved henting av tilganger for ident " + ident, exception);
            throw new TiltaksgjennomforingException("Feil ved tilgangskontrollsjekk for ident " + ident.asString(), exception);
        }
    }
}