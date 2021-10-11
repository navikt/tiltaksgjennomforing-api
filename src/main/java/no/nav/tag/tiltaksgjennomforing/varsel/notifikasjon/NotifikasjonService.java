package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.request.ArbeidsgiverMutationRequest;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.request.Variables;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.MutationStatus;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.nyBeskjed.NyBeskjedResponse;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.nyOppgave.NyOppgaveResponse;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.oppgaveUtfoert.OppgaveUtfoertResponse;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.softDeleteNotifikasjon.SoftDeleteNotifikasjonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;


@Slf4j
@Component
public class NotifikasjonService {
    private final RestTemplate restTemplate;
    private final NotifikasjonHandler handler;
    private final NotifikasjonerProperties notifikasjonerProperties;
    private final NotifikasjonParser notifikasjonParser;

    private final ArbeidsgiverNotifikasjonRepository arbeidsgiverNotifikasjonRepository;

    public NotifikasjonService(
            @Qualifier("påVegneAvSaksbehandlerGraphRestTemplate") RestTemplate restTemplate,
            @Autowired NotifikasjonParser notifikasjonParser,
            NotifikasjonerProperties properties,
            NotifikasjonHandler handler,
            @Autowired ArbeidsgiverNotifikasjonRepository arbeidsgiverNotifikasjonRepository
    ) {
        this.restTemplate = restTemplate;
        this.notifikasjonerProperties = properties;
        this.notifikasjonParser = notifikasjonParser;
        this.handler = handler;
        this.arbeidsgiverNotifikasjonRepository = arbeidsgiverNotifikasjonRepository;
    }

    private HttpEntity<String> createRequestEntity(ArbeidsgiverMutationRequest arbeidsgiverMutationRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity(arbeidsgiverMutationRequest, headers);
    }

    public String opprettNotifikasjon(ArbeidsgiverMutationRequest arbeidsgiverMutationRequest) {
        try {
            return restTemplate.postForObject(
                    notifikasjonerProperties.getUri(),
                    createRequestEntity(arbeidsgiverMutationRequest),
                    String.class);
        } catch (RestClientException exception) {
            log.error("Feil med sending av notifikasjon: ", exception);
            throw exception;
        }
    }

    public String getAvtaleLenke(Avtale avtale) {
        return notifikasjonerProperties.getLenke().concat("?").concat(avtale.getBedriftNr().asString());
    }

    private String opprettNyMutasjon(ArbeidsgiverNotifikasjon notifikasjon, String mutation, String merkelapp, String tekst) {
        Variables variables = new Variables();
        variables.setEksternId(notifikasjon.getId().toString());
        variables.setVirksomhetsnummer(notifikasjon.getVirksomhetsnummer().asString());
        variables.setLenke(notifikasjon.getLenke());
        variables.setServiceCode(notifikasjon.getServiceCode().toString());
        variables.setMerkelapp(merkelapp);
        variables.setTekst(tekst);
        variables.setServiceEdition(notifikasjon.getServiceEdition().toString());
        ArbeidsgiverMutationRequest request = new ArbeidsgiverMutationRequest(
                mutation,
                variables);
        return opprettNotifikasjon(request);
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
        final NyBeskjedResponse beskjed = handler.readResponse(response, NyBeskjedResponse.class);
        handler.sjekkOgSettStatusResponse(
                notifikasjon,
                handler.convertResponse(beskjed.getData().getNyBeskjed()),
                MutationStatus.NY_BESKJED_VELLYKKET);
        return beskjed;
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
        final NyOppgaveResponse oppgave = handler.readResponse(response, NyOppgaveResponse.class);
        handler.sjekkOgSettStatusResponse(
                notifikasjon,
                handler.convertResponse(oppgave.getData().getNyOppgave()),
                MutationStatus.NY_OPPGAVE_VELLYKKET);
        return oppgave;
    }

    // TODO: lag metode som henter ut liste av alle oppgaver/beskjeder som er aktiv. iterer over dem og send beskjed til notifikasjon API at oppgaveUtfoert ved bruk av metode under.
    // TODO: skriv ferdig metode

    public OppgaveUtfoertResponse oppgaveUtfoert(ArbeidsgiverNotifikasjon notifikasjon, NotifikasjonMerkelapp merkelapp, NotifikasjonTekst tekst){
        final String response = opprettNyMutasjon(
                notifikasjon,
                notifikasjonParser.getOppgaveUtfoert(),
                merkelapp.getValue(),
                tekst.getTekst());
        final OppgaveUtfoertResponse utfortOppgave = handler.readResponse(response, OppgaveUtfoertResponse.class);
        handler.sjekkOgSettStatusResponse(
                notifikasjon,
                handler.convertResponse(utfortOppgave.getData().getOppgaveUtfoert()),
                MutationStatus.OPPGAVE_UTFOERT_VELLYKKET
        );
        return utfortOppgave;
    }

    public SoftDeleteNotifikasjonResponse softDeleteNotifikasjon(
            ArbeidsgiverNotifikasjon notifikasjon,
            NotifikasjonMerkelapp merkelapp,
            NotifikasjonTekst tekst) {
        final String response = opprettNyMutasjon(
                notifikasjon,
                notifikasjonParser.getSoftDeleteNotifikasjon(),
                merkelapp.getValue(),
                tekst.getTekst());
        final SoftDeleteNotifikasjonResponse softDelete = handler.readResponse(response, SoftDeleteNotifikasjonResponse.class);
        handler.sjekkOgSettStatusResponse(
                notifikasjon,
                handler.convertResponse(softDelete.getData().getSoftDeleteNotifikasjon()),
                MutationStatus.SOFT_DELETE_NOTIFIKASJON_VELLYKKET
        );
        return softDelete;
    }

    public void sendBeskjedAtOppgaveUtfort(ArbeidsgiverNotifikasjon notifikasjon, NotifikasjonMerkelapp merkelapp, NotifikasjonTekst tekst){
        for(ArbeidsgiverNotifikasjon notifikasjoner : arbeidsgiverNotifikasjonRepository.aktiveNotifikasjoner(notifikasjon.getAvtaleId())) {
            oppgaveUtfoert(notifikasjon, merkelapp, tekst);
        }
    public void oppgaveUtfoert(UUID avtaleId) {
        final List<ArbeidsgiverNotifikasjon> notifikasjonList =
                handler.finnAktiveNotifikasjonerUtfoert(avtaleId);
        if (!notifikasjonList.isEmpty()) {
            notifikasjonList.forEach(n -> {
                Variables variables = new Variables();
                variables.setId(n.getId());
                opprettNotifikasjon(new ArbeidsgiverMutationRequest(
                        notifikasjonParser.getOppgaveUtfoert(),
                        variables
                ));
                n.setOppgaveAvsluttet(true);
                handler.saveNotifikasjon(n);
            });
        }
    }

    public void softDeleteNotifikasjoner(Avtale avtale) {
        final List<ArbeidsgiverNotifikasjon> notifikasjonlist =
                handler.finnAktiveNotifikasjonerUtfoert(avtale.getId());
        if (!notifikasjonlist.isEmpty()) {
            notifikasjonlist.forEach(n -> {
                Variables variables = new Variables();
                variables.setId(n.getId());
                opprettNotifikasjon(new ArbeidsgiverMutationRequest(
                        notifikasjonParser.getSoftDeleteNotifikasjon(),
                        variables));
                n.setOppgaveAvsluttet(true);
                handler.saveNotifikasjon(n);
            });
        }
    }
}
