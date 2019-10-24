package no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.CorrelationIdSupplier;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.restservicecache.CacheConfiguration;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(CacheConfiguration.AXSYS_CACHE)
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
            log.warn("Feil ved henting av enheter for ident " + ident, exception);
            throw exception;
        }
    }
    
    @CacheEvict(cacheNames=CacheConfiguration.AXSYS_CACHE, allEntries=true)
    public void cacheEvict() {
    }

}