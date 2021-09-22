package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.adapter;

import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.restservicecache.CacheConfiguration;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.sts.STSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AbacAdapter {
  final Logger log = LoggerFactory.getLogger(this.getClass());
  private final RestTemplate restTemplate;

  private final STSClient stsClient;

  private final AbacProperties abacProperties;

  public boolean harLeseTilgang(NavIdent navIdent, Fnr deltakerFnr, AbacAction action){
    try{
      AbacResponse response = restTemplate.postForObject(abacProperties.getUri(), getHttpEntity(
          AbacTransformer.tilAbacRequestGittNavIdentOgDeltakerFnr(navIdent.asString(),deltakerFnr.asString(),action.toString())),AbacResponse.class);
      log.info("############ harLeseTilgang... {}", response.response.decision);
      return Objects.equals(response.response.decision, "Permit");
    }catch (RuntimeException ex){
      throw new TilgangskontrollException("Feil fra abac: " + ex);
    }
  }


  private HttpEntity getHttpEntity(String body){
    HttpHeaders headers = new HttpHeaders();
    headers.set("Nav-Consumer-Id","tiltak-refusjon-api");
    headers.set("Nav-Call-Id", UUID.randomUUID().toString());
    headers.set("Content-Type","application/json");
    headers.setBearerAuth(stsClient.hentSTSToken().getAccessToken());
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    return new HttpEntity<>(body,headers);
  }

  @CacheEvict(cacheNames= CacheConfiguration.ABAC_CACHE, allEntries=true)
  public void cacheEvict() {
  }

}
