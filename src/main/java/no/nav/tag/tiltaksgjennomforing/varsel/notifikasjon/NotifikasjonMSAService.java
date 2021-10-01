package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringProperties;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.request.ArbeidsgiverMutationRequest;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.request.Variables;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.nyBeskjed.NyBeskjedResponse;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.nyOppgave.NyOppgaveResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class NotifikasjonMSAService {
    private final RestTemplate restTemplate;
    private final AltinnTilgangsstyringProperties altinnTilgangsstyringProperties;
    private final NotifikasjonerProperties notifikasjonerProperties;
    private final String nyOppgave;
    private final String nyBeskjed;
    private final ObjectMapper objectMapper;
    private final ArbeidsgiverNotifikasjonRepository arbeidsgiverNotifikasjonRepository;

    public NotifikasjonMSAService(
            @Qualifier("påVegneAvSaksbehandlerGraphRestTemplate") RestTemplate restTemplate,
            AltinnTilgangsstyringProperties altinnTilgangsstyringProperties,
            NotifikasjonerProperties properties,
            @Value("classpath:varsler/opprettNyOppgave.graphql") Resource nyOppgave,
            @Value("classpath:varsler/opprettNyBeskjed.graphql") Resource nyBeskjed,
            @Autowired ObjectMapper objectMapper,
            ArbeidsgiverNotifikasjonRepository repository
    ) {
        this.restTemplate = restTemplate;
        this.altinnTilgangsstyringProperties = altinnTilgangsstyringProperties;
        this.notifikasjonerProperties = properties;
        this.nyOppgave = resourceAsString(nyOppgave);
        this.nyBeskjed = resourceAsString(nyBeskjed);
        this.objectMapper = objectMapper;
        this.arbeidsgiverNotifikasjonRepository = repository;
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

    public String opprettNotifikasjon(ArbeidsgiverMutationRequest arbeidsgiverMutationRequest, Notifikasjon notifikasjon) {
        try {
            String response = restTemplate.postForObject(
                    notifikasjonerProperties.getUri(),
                    createRequestEntity(arbeidsgiverMutationRequest),
                    String.class);
            notifikasjon.setHendelseUtfort(true);
            arbeidsgiverNotifikasjonRepository.save(notifikasjon);
            return response;
        } catch (RestClientException exception) {
            log.error("Feil med sending av notifikasjon: ", exception);
            throw exception;
        }

    }

    public String getAvtaleLenke(Avtale avtale) {
        return notifikasjonerProperties.getLenke().concat("?").concat(avtale.getBedriftNr().asString());
    }

    public AltinnNotifikasjonsProperties getNotifikasjonerProperties(Avtale avtale) {
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
            default:
                return new AltinnNotifikasjonsProperties(
                        altinnTilgangsstyringProperties.getLtsVarigServiceCode(),
                        altinnTilgangsstyringProperties.getLtsVarigServiceEdition());
        }
    }

    private String opprettNyMutasjon(Notifikasjon notifikasjon, String mutation, String merkelapp, String tekst) {
        ArbeidsgiverMutationRequest request = new ArbeidsgiverMutationRequest(
                mutation,
                new Variables(
                        notifikasjon.getId().toString(),
                        notifikasjon.getVirksomhetsnummer().asString(),
                        notifikasjon.getLenke(),
                        notifikasjon.getServiceCode().toString(),
                        notifikasjon.getServiceEdition().toString(),
                        merkelapp,
                        tekst));

        return opprettNotifikasjon(request, notifikasjon);
    }

    public NyBeskjedResponse opprettNyBeskjed(
            Notifikasjon notifikasjon,
            NotifikasjonMerkelapp merkelapp,
            NotifikasjonTekst tekst) throws JsonProcessingException {
        final String response = opprettNyMutasjon(
                notifikasjon,
                nyBeskjed,
                merkelapp.getValue(),
                tekst.getTekst());
        return objectMapper.readValue(response, NyBeskjedResponse.class);
    }

    public NyOppgaveResponse opprettOppgave(
            Notifikasjon notifikasjon,
            NotifikasjonMerkelapp merkelapp,
            NotifikasjonTekst tekst) throws JsonProcessingException {
        final String response = opprettNyMutasjon(
                notifikasjon,
                nyOppgave,
                merkelapp.getValue(),
                tekst.getTekst());
        return objectMapper.readValue(response, NyOppgaveResponse.class);
    }
}
