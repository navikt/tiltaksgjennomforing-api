package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.sts.STSClient;
import no.nav.tag.tiltaksgjennomforing.persondata.Variables;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class Notifikasjon {
    private final RestTemplate restTemplate;
    private final STSClient stsClient;
    private final NotifikasjonerProperties notifikasjonerProperties;

    @Value("classpath:graphql/whoAmI.graphql")
    private Resource notifikajonerQueryResource;

    @SneakyThrows
    private static String resourceAsString(Resource adressebeskyttelseQuery) {
        String filinnhold = StreamUtils.copyToString(adressebeskyttelseQuery.getInputStream(), StandardCharsets.UTF_8);
        return filinnhold.replaceAll("\\s+", " ");
    }

    private HttpEntity<String> createRequestEntity(NotifikasjonerArbeidsgiverRequest notifikasjonerArbeidsgiverRequest) {
        String stsToken = stsClient.hentSTSToken().getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(stsToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Tema", "GEN");
        headers.set("Nav-Consumer-Token", "Bearer " + stsToken);
        return new HttpEntity(notifikasjonerArbeidsgiverRequest, headers);
    }

    public NotifikasjonerRespons utførKallTilNotifikasjoner (NotifikasjonerArbeidsgiverRequest notifikasjonerArbeidsgiverRequest) {
        try {
            return restTemplate.postForObject(notifikasjonerProperties.getUri(), createRequestEntity(notifikasjonerArbeidsgiverRequest), NotifikasjonerRespons.class);
        }
        catch (RestClientException exception) {
            stsClient.evictToken();
            log.error("Feil fra Notifikasjoner med request-url: ", exception);
            throw exception;
        }
    }

    public String whoAmI () {
        NotifikasjonerArbeidsgiverRequest notifikasjonerArbeidsgiverRequest = new NotifikasjonerArbeidsgiverRequest(resourceAsString(notifikajonerQueryResource));
        return hentWhoAmIFraNotifikasjonRespons(utførKallTilNotifikasjoner(notifikasjonerArbeidsgiverRequest));
    }

    private static String hentWhoAmIFraNotifikasjonRespons(NotifikasjonerRespons notifikasjonerRespons) {
        try {
            return notifikasjonerRespons.getData().getWhoAmI().getWhoAmI();
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            return "";
        }
    }
}
