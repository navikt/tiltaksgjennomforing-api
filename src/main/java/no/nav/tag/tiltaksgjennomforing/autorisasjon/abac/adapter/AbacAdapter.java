package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.adapter;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.caching.CacheConfiguration;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.sts.STSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.adapter.AbacTransformer.tilAbacRequestBody;

@Service
@RequiredArgsConstructor
public class AbacAdapter {
    final Logger log = LoggerFactory.getLogger(getClass());
    private final RestTemplate restTemplate;

    private final STSClient stsClient;

    private final AbacProperties abacProperties;

    @Cacheable(CacheConfiguration.ABAC_CACHE)
    public boolean harLeseTilgang(NavIdent navIdent, Fnr deltakerFnr) {
        try {
            AbacResponse response = restTemplate.postForObject(
                    abacProperties.getUri(),
                    getHttpEntity(tilAbacRequestBody(navIdent.asString(), deltakerFnr.asString())),
                    AbacResponse.class
            );
            return Objects.equals(response.response.decision, "Permit");
        } catch (RuntimeException ex) {
            log.error("Abac feil", ex);
            return false;
        }
    }


    private HttpEntity getHttpEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Nav-Consumer-Id", abacProperties.getNavConsumerId());
        headers.set("Nav-Call-Id", UUID.randomUUID().toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(stsClient.hentSTSToken().getAccessToken());
        return new HttpEntity<>(body, headers);
    }

    // @CacheEvict(cacheNames = CacheConfiguration.ABAC_CACHE, allEntries = true)
    public void cacheEvict() {
    }

}
