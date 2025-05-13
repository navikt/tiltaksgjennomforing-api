package no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.CorrelationIdSupplier;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AxsysService {
    private final String baseUrl;
    private final String navConsumerId;
    private final RestTemplate restTemplate;

    public AxsysService(AxsysProperties axsysProperties, RestTemplate noAuthRestTemplate) {
        this.baseUrl = axsysProperties.getUri().toString();
        this.navConsumerId = axsysProperties.getNavConsumerId();
        this.restTemplate = noAuthRestTemplate;
    }

    @Cacheable(CacheConfig.AXSYS_CACHE)
    public List<NavEnhet> hentEnheterNavAnsattHarTilgangTil(NavIdent ident) {
        HttpHeaders headers = new HttpHeaders();
        var callId = CorrelationIdSupplier.get();
        if (callId != null) {
            headers.set("Nav-Call-Id", callId);
        }
        var consumerId = navConsumerId;
        if (consumerId != null) {
            headers.set("Nav-Consumer-Id", consumerId);
        }

        try {
            AxsysRespons respons = restTemplate.exchange(
                baseUrl + "/{navIdent}?inkluderAlleEnheter=false",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                AxsysRespons.class,
                Map.of("navIdent", ident.asString())
            ).getBody();
            return respons.tilEnheter();
        } catch (HttpClientErrorException.NotFound klientfeil) {
            // Nav-identer kan mangle informasjon i Axsys
            // (feks test-ident opprettet i IDA hvor Axsys-info ikke er fylt ut)
            log.warn("Nav-veileder {} ble ikke funnet i axsys", ident);
            return List.of();
        } catch (RestClientException exception) {
            log.warn("Feil ved henting av enheter for ident {}", ident, exception);
            throw exception;
        }
    }

    @CacheEvict(cacheNames = CacheConfig.AXSYS_CACHE, allEntries = true)
    public void cacheEvict() {
        log.info("Tømmer axsys cache for data");
    }

}
