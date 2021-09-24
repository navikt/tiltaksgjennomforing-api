package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
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
@Profile({Miljø.PROD_FSS, Miljø.DEV_FSS})
public class NotifikasjonMSAService {
    private final RestTemplate restTemplate;
    private final NotifikasjonerProperties notifikasjonerProperties;
    private final Resource notifikajonerQueryResource;

    public NotifikasjonMSAService(
            @Qualifier("påVegneAvSaksbehandlerGraphRestTemplate") RestTemplate restTemplate,
            NotifikasjonerProperties properties,
            @Value("classpath:varsler/whoAmI.graphql") Resource notifikajonerQueryResource) {
        this.restTemplate = restTemplate;
        this.notifikasjonerProperties = properties;
        this.notifikajonerQueryResource = notifikajonerQueryResource;
    }

    @SneakyThrows
    private static String resourceAsString(Resource adressebeskyttelseQuery) {
        String filinnhold = StreamUtils.copyToString(adressebeskyttelseQuery.getInputStream(), StandardCharsets.UTF_8);
        return filinnhold.replaceAll("\\s+", " ");
    }

    private HttpEntity<String> createRequestEntity(NotifikasjonerArbeidsgiverRequest notifikasjonerArbeidsgiverRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Tema", "GEN");
        return new HttpEntity(notifikasjonerArbeidsgiverRequest, headers);
    }

    public NotifikasjonerRespons utførKallTilNotifikasjoner (NotifikasjonerArbeidsgiverRequest notifikasjonerArbeidsgiverRequest) {
        try {
            return restTemplate.postForObject(notifikasjonerProperties.getUri(), createRequestEntity(notifikasjonerArbeidsgiverRequest), NotifikasjonerRespons.class);
        }
        catch (RestClientException exception) {
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
