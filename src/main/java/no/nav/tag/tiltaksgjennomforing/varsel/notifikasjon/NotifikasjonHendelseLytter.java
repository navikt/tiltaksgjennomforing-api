package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.events.*;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotifikasjonHendelseLytter {
    private final ArbeidsgiverNotifikasjonRepository arbeidsgiverNotifikasjonRepository;

    @Autowired
    NotifikasjonMSAService notifikasjonMSAService;

    @EventListener
    public void avtaleOpprettet(AvtaleOpprettetAvVeileder event) throws JsonProcessingException {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(), VarslbarHendelseType.OPPRETTET, notifikasjonMSAService);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonMSAService.opprettNyBeskjed(
                notifikasjon,
                NotifikasjonMerkelapp.TILTAK_AVTALE_OPPRETTET,
                NotifikasjonTekst.AVTALE_OPPRETTET);
    }

    @EventListener
    public void avtaleKlarForRefusjon(RefusjonKlar event) throws JsonProcessingException {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(), VarslbarHendelseType.REFUSJON_KLAR, notifikasjonMSAService);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonMSAService.opprettOppgave(
                notifikasjon,
                NotifikasjonMerkelapp.TILTAK_AVTALE_KLAR_REFUSJON,
                NotifikasjonTekst.TILTAK_AVTALE_KLAR_REFUSJON
        );
    }

    @EventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) throws JsonProcessingException {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENT_AV_VEILEDER, notifikasjonMSAService);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonMSAService.opprettNyBeskjed(
                notifikasjon,
                NotifikasjonMerkelapp.TILTAK_AVTALE_GODKJENT_VEILEDER,
                NotifikasjonTekst.TILTAK_AVTALE_GODKJENT_VEILEDER);
    }

    @EventListener
    public void avtaleEndret(AvtaleEndret event) throws JsonProcessingException {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(), VarslbarHendelseType.ENDRET, notifikasjonMSAService);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonMSAService.opprettOppgave(
                notifikasjon,
                NotifikasjonMerkelapp.TILTAK_AVTALE_ENDRET,
                NotifikasjonTekst.TILTAK_AVTALE_ENDRET
        );
    }

    @EventListener
    public void avtaleInngått(AvtaleInngått event) {

    }

    @EventListener
    public void avbrutt(AvbruttAvVeileder event) {

    }

    @EventListener
    public void låstOpp(AvtaleLåstOpp event) {

    }

    @EventListener
    public void gjenopprettet(AvtaleGjenopprettet event) {

    }
}
