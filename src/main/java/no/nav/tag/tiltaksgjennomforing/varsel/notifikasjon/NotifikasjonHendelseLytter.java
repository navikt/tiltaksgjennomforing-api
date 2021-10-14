package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.events.*;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelseType;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.MutationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@ConditionalOnProperty("tiltaksgjennomforing.notifikasjonHendelseLytter.enabled")
public class NotifikasjonHendelseLytter {
    private final ArbeidsgiverNotifikasjonRepository arbeidsgiverNotifikasjonRepository;

    @Autowired
    private final NotifikasjonService notifikasjonService;

    @Autowired
    private final NotifikasjonParser parser;

   @EventListener
    public void avtaleOpprettet(AvtaleOpprettetAvVeileder event) {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(), VarslbarHendelseType.OPPRETTET, notifikasjonService, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonService.opprettOppgave(
                notifikasjon,
                NotifikasjonMerkelapp.getMerkelapp(event.getAvtale().getTiltakstype().getBeskrivelse()),
                NotifikasjonTekst.AVTALE_OPPRETTET);
    }

    @EventListener
    public void avtaleKlarForRefusjon(RefusjonKlar event) {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(), VarslbarHendelseType.REFUSJON_KLAR, notifikasjonService, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonService.opprettNyBeskjed(
                notifikasjon,
                NotifikasjonMerkelapp.getMerkelapp(event.getAvtale().getTiltakstype().getBeskrivelse()),
                NotifikasjonTekst.TILTAK_AVTALE_KLAR_REFUSJON
        );
    }

@EventListener
    public void godkjentAvArbeidsgiver(GodkjentAvArbeidsgiver event) {
        notifikasjonService.oppgaveUtfoert(
                event.getAvtale().getId(),
                VarslbarHendelseType.OPPRETTET,
                MutationStatus.NY_OPPGAVE_VELLYKKET);
    }

    @EventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(
                        event.getAvtale(),
                        VarslbarHendelseType.GODKJENT_AV_VEILEDER,
                        notifikasjonService,
                        parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonService.oppgaveUtfoert(
                event.getAvtale().getId(),
                VarslbarHendelseType.OPPRETTET,
                MutationStatus.NY_OPPGAVE_VELLYKKET);
        notifikasjonService.opprettNyBeskjed(
                notifikasjon,
                NotifikasjonMerkelapp.getMerkelapp(event.getAvtale().getTiltakstype().getBeskrivelse()),
                NotifikasjonTekst.TILTAK_AVTALE_GODKJENT_VEILEDER);
    }

    @EventListener
    public void avtaleInngått(AvtaleInngått event) {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(
                        event.getAvtale(),
                        VarslbarHendelseType.AVTALE_INNGÅTT,
                        notifikasjonService,
                        parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonService.opprettOppgave(
                notifikasjon,
                NotifikasjonMerkelapp.getMerkelapp(event.getAvtale().getTiltakstype().getBeskrivelse()),
                NotifikasjonTekst.TILTAK_AVTALE_INNGATT
        );
    }

    @EventListener
    public void sletteNotifikasjon(AvtaleSlettemerket event) {
        notifikasjonService.softDeleteNotifikasjoner(event.getAvtale());
    }

    @EventListener
    public void avtaleAnnullert(AnnullertAvVeileder event) {
        notifikasjonService.softDeleteNotifikasjoner(event.getAvtale());
    }
}
