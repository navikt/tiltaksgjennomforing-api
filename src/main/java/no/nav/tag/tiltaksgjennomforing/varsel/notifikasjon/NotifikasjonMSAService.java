package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringProperties;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.request.ArbeidsgiverMutationRequest;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.request.Variables;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.opprettNotifikasjonResponse;
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
    private final AltinnTilgangsstyringProperties altinnTilgangsstyringProperties;
    private final NotifikasjonerProperties notifikasjonerProperties;
    private final String nyOppgave;
    private final String nyBeskjed;

    public NotifikasjonMSAService(
            @Qualifier("påVegneAvSaksbehandlerGraphRestTemplate") RestTemplate restTemplate,
            AltinnTilgangsstyringProperties altinnTilgangsstyringProperties,
            NotifikasjonerProperties properties,
            @Value("classpath:varsler/opprettNyOppgave.graphql") Resource nyOppgave,
            @Value("classpath:varsler/opprettNyBeskjed.graphql") Resource nyBeskjed
    ) {
        this.restTemplate = restTemplate;
        this.altinnTilgangsstyringProperties = altinnTilgangsstyringProperties;
        this.notifikasjonerProperties = properties;
        this.nyOppgave = resourceAsString(nyOppgave);
        this.nyBeskjed = resourceAsString(nyBeskjed);
    }

    @SneakyThrows
    private static String resourceAsString(Resource adressebeskyttelseQuery) {
        String filinnhold = StreamUtils.copyToString(adressebeskyttelseQuery.getInputStream(), StandardCharsets.UTF_8);
        return filinnhold.replaceAll("\\s+", " ");
    }

    private HttpEntity<String> createRequestEntity(ArbeidsgiverMutationRequest arbeidsgiverMutationRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity(arbeidsgiverMutationRequest, headers);
    }

    public opprettNotifikasjonResponse opprettNotifikasjon (ArbeidsgiverMutationRequest arbeidsgiverMutationRequest) {
        try {
            return restTemplate.postForObject(
                    notifikasjonerProperties.getUri(),
                    createRequestEntity(arbeidsgiverMutationRequest),
                    opprettNotifikasjonResponse.class);
        }
        catch (RestClientException exception) {
            log.error("Feil fra Notifikasjoner med request-url: ", exception);
            throw exception;
        }
    }

    private String getAvtaleLenke(Avtale avtale) {
        return notifikasjonerProperties.getLenke().concat("?").concat(avtale.getBedriftNr().asString());
    }

    private AltinnNotifikasjonsProperties getNotifikasjonerProperties(Avtale avtale) {
        switch (avtale.getTiltakstype()) {
            case MIDLERTIDIG_LONNSTILSKUDD:
                return new AltinnNotifikasjonsProperties(
                        altinnTilgangsstyringProperties.getLtsMidlertidigServiceCode(),
                        altinnTilgangsstyringProperties.getLtsMidlertidigServiceEdition());
            case ARBEIDSTRENING:
                return new AltinnNotifikasjonsProperties(
                        altinnTilgangsstyringProperties.getArbtreningServiceCode(),
                        altinnTilgangsstyringProperties.getArbtreningServiceEdition());
            case SOMMERJOBB:
                return new AltinnNotifikasjonsProperties(
                        altinnTilgangsstyringProperties.getSommerjobbServiceCode(),
                        altinnTilgangsstyringProperties.getSommerjobbServiceEdition());
            default: return new AltinnNotifikasjonsProperties(
                    altinnTilgangsstyringProperties.getLtsVarigServiceCode(),
                    altinnTilgangsstyringProperties.getLtsVarigServiceEdition());
        }
    }

    private opprettNotifikasjonResponse opprettNyMutasjon(
            Avtale avtale,
            String mutation,
            String serviceCode,
            String serviceEdition,
            String merkelapp,
            String tekst) {
        ArbeidsgiverMutationRequest request = new ArbeidsgiverMutationRequest(
                mutation,
                new Variables(
                        avtale.getId().toString(),
                        avtale.getBedriftNr().asString(),
                        getAvtaleLenke(avtale),
                        serviceCode,
                        serviceEdition,
                        merkelapp,
                        tekst));

        return opprettNotifikasjon(request);
    }

    public opprettNotifikasjonResponse opprettNyBeskjed(Avtale avtale, NotifikasjonMerkelapp merkelapp, NotifikasjonTekst tekst) {
        AltinnNotifikasjonsProperties properties = getNotifikasjonerProperties(avtale);
        return opprettNyMutasjon(
                avtale,
                nyBeskjed,
                properties.getServiceCode().toString(),
                properties.getServiceEdition().toString(),
                merkelapp.getValue(),
                tekst.getTekst());
    }

    public opprettNotifikasjonResponse opprettOppgave(Avtale avtale, NotifikasjonMerkelapp merkelapp, NotifikasjonTekst tekst) {
        AltinnNotifikasjonsProperties properties = getNotifikasjonerProperties(avtale);
        return opprettNyMutasjon(
                avtale,
                nyBeskjed,
                properties.getServiceCode().toString(),
                properties.getServiceEdition().toString(),
                merkelapp.getValue(),
                tekst.getTekst());
    }
}
