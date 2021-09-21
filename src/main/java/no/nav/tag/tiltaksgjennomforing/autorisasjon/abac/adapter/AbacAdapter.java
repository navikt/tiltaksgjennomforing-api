package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.adapter;

import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AbacAdapter {

  private final RestTemplate restTemplate;

  private final AbacConfig abacConfig;

  public boolean harLeseTilgang(NavIdent navIdent, Fnr deltakerFnr){
    AbacResponse response = restTemplate.postForObject(abacConfig.getUri(), getHttpEntity(
        AbacTransformer.tilAbacRequestGittNavIdentOgDeltakerFnr(navIdent.asString(),deltakerFnr.asString())),AbacResponse.class);
    return Objects.equals(response.response.decision, "Permit");
  }


  private HttpEntity getHttpEntity(String body){

    //TODO: STS
    HttpHeaders headers = new HttpHeaders();
    headers.set("Nav-Consumer-Id","tiltak-refusjon-api");
    headers.set("Nav-Call-Id", UUID.randomUUID().toString());
    headers.set("Content-Type","application/json");
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    return new HttpEntity<>(body,headers);
  }


}
