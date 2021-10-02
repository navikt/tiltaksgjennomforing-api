package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.request.ArbeidsgiverMutationRequest;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.request.Variables;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.nyBeskjed.NyBeskjedResponse;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.nyOppgave.NyOppgaveResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Component
public class NotifikasjonMSAService {
    private final RestTemplate restTemplate;

    private final NotifikasjonerProperties notifikasjonerProperties;
    private final NotifikasjonParser notifikasjonParser;
    private final ObjectMapper objectMapper;
    private final ArbeidsgiverNotifikasjonRepository arbeidsgiverNotifikasjonRepository;

    public NotifikasjonMSAService(
            @Qualifier("påVegneAvSaksbehandlerGraphRestTemplate") RestTemplate restTemplate,
            @Autowired NotifikasjonParser notifikasjonParser,
            @Autowired ObjectMapper objectMapper,
            NotifikasjonerProperties properties,
            ArbeidsgiverNotifikasjonRepository repository
    ) {
        this.restTemplate = restTemplate;
        this.notifikasjonerProperties = properties;
        this.notifikasjonParser = notifikasjonParser;
        this.objectMapper = objectMapper;
        this.arbeidsgiverNotifikasjonRepository = repository;
    }

    private HttpEntity<String> createRequestEntity(ArbeidsgiverMutationRequest arbeidsgiverMutationRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity(arbeidsgiverMutationRequest, headers);
    }

    public String opprettNotifikasjon(ArbeidsgiverMutationRequest arbeidsgiverMutationRequest, ArbeidsgiverNotifikasjon notifikasjon) {
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

    private String opprettNyMutasjon(ArbeidsgiverNotifikasjon notifikasjon, String mutation, String merkelapp, String tekst) {
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
            ArbeidsgiverNotifikasjon notifikasjon,
            NotifikasjonMerkelapp merkelapp,
            NotifikasjonTekst tekst) {
        final String response = opprettNyMutasjon(
                notifikasjon,
                notifikasjonParser.getNyBeskjed(),
                merkelapp.getValue(),
                tekst.getTekst());
        try {
            objectMapper.readValue(response, NyBeskjedResponse.class);
        }catch (JsonProcessingException exception) {
            log.error("opprett nybeskjed err: {}", exception.getMessage());
        }
        return null;
    }

    public NyOppgaveResponse opprettOppgave(
            ArbeidsgiverNotifikasjon notifikasjon,
            NotifikasjonMerkelapp merkelapp,
            NotifikasjonTekst tekst) {
        final String response = opprettNyMutasjon(
                notifikasjon,
                notifikasjonParser.getNyOppgave(),
                merkelapp.getValue(),
                tekst.getTekst());
        try {
            return objectMapper.readValue(response, NyOppgaveResponse.class);
        }catch (JsonProcessingException exception) {
            log.error("opprettoppgave obj mapper err: {}", exception.getMessage());
        }
        return null;
    }
}
