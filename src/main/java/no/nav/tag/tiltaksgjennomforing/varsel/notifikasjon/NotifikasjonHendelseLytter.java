package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

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

    //TODO legg til @EventListener på SoftDelete av oppgaver/beskjeder når avtale har blitt slettet av veileder.

    @Autowired
    NotifikasjonService notifikasjonService;

    @Autowired
    NotifikasjonParser parser;

    @EventListener
    public void avtaleOpprettet(AvtaleOpprettetAvVeileder event) {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(), VarslbarHendelseType.OPPRETTET, notifikasjonService, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonService.opprettOppgave(
                notifikasjon,
                NotifikasjonMerkelapp.TILTAK,
                NotifikasjonTekst.AVTALE_OPPRETTET);
    }

    @EventListener
    public void avtaleKlarForRefusjon(RefusjonKlar event) {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(), VarslbarHendelseType.REFUSJON_KLAR, notifikasjonService, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonService.opprettOppgave(
                notifikasjon,
                NotifikasjonMerkelapp.REFUSJON,
                NotifikasjonTekst.TILTAK_AVTALE_KLAR_REFUSJON
        );
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
        notifikasjonService.opprettNyBeskjed(
                notifikasjon,
                NotifikasjonMerkelapp.TILTAK,
                NotifikasjonTekst.TILTAK_AVTALE_GODKJENT_VEILEDER);
    }

    @EventListener
    public void avtaleEndret(AvtaleEndret event) {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(), VarslbarHendelseType.ENDRET, notifikasjonService, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonService.opprettNyBeskjed(
                notifikasjon,
                NotifikasjonMerkelapp.TILTAK,
                NotifikasjonTekst.TILTAK_AVTALE_ENDRET
        );
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
                NotifikasjonMerkelapp.TILTAK,
                NotifikasjonTekst.TILTAK_AVTALE_INNGATT
        );
    }

    @EventListener
    public void avbrutt(AvbruttAvVeileder event) {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(), VarslbarHendelseType.AVBRUTT, notifikasjonService, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        // TODO: bruk metode som sjekker tabell og sjekker om det finnes aktive notifikasjons-oppgaver i tabell. For så å sende bekjed at oppgaveUtfoert()
        notifikasjonService.sendBeskjedAtOppgaveUtfort(notifikasjon,NotifikasjonMerkelapp.TILTAK, NotifikasjonTekst.TILTAK_NOTIFIKASJON_OPPGAVE_UTFØRT);
        notifikasjonService.opprettNyBeskjed(
                notifikasjon,
                NotifikasjonMerkelapp.TILTAK,
                NotifikasjonTekst.TILTAK_AVTALE_AVBRUTT
        );
    }

    @EventListener
    public void låstOpp(AvtaleLåstOpp event) {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(), VarslbarHendelseType.LÅST_OPP, notifikasjonService, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonService.opprettOppgave(
                notifikasjon,
                NotifikasjonMerkelapp.TILTAK,
                NotifikasjonTekst.TILTAK_AVTALE_LASTOPP
        );
    }

    @EventListener
    public void gjenopprettet(AvtaleGjenopprettet event) {
            final ArbeidsgiverNotifikasjon notifikasjon =
                    ArbeidsgiverNotifikasjon.nyHendelse(
                            event.getAvtale(),
                            VarslbarHendelseType.GJENOPPRETTET,
                            notifikasjonService,
                            parser);
            arbeidsgiverNotifikasjonRepository.save(notifikasjon);
            notifikasjonService.opprettOppgave(
                    notifikasjon,
                    NotifikasjonMerkelapp.TILTAK,
                    NotifikasjonTekst.TILTAK_AVTALE_GJENOPPRETTET
            );
    }

    @EventListener
    public void sletteNotifikasjon(AvtaleSlettemerket event) {
            final ArbeidsgiverNotifikasjon notifikasjon = ArbeidsgiverNotifikasjon.nyHendelse(
                    event.getAvtale(), VarslbarHendelseType.AVTALE_SLETTET, notifikasjonService, parser);
            arbeidsgiverNotifikasjonRepository.save(notifikasjon);
            notifikasjonService.softDeleteNotifikasjon(
                    notifikasjon,
                    NotifikasjonMerkelapp.TILTAK,
                    NotifikasjonTekst.TILTAK_AVTALE_SLETTET_VEILEDER
            );
    }
}
