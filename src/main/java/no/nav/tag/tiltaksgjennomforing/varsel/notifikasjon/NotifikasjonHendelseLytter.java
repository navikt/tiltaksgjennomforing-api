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

    @Autowired
    NotifikasjonService service;

    @Autowired
    NotifikasjonParser parser;

    @EventListener
    public void avtaleOpprettet(AvtaleOpprettetAvVeileder event) {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(), VarslbarHendelseType.OPPRETTET, service, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        service.opprettOppgave(
                notifikasjon,
                NotifikasjonMerkelapp.TILTAK,
                NotifikasjonTekst.AVTALE_OPPRETTET);
    }

    @EventListener
    public void avtaleKlarForRefusjon(RefusjonKlar event) {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(), VarslbarHendelseType.REFUSJON_KLAR, service, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        service.opprettOppgave(
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
                        service,
                        parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        service.opprettNyBeskjed(
                notifikasjon,
                NotifikasjonMerkelapp.TILTAK,
                NotifikasjonTekst.TILTAK_AVTALE_GODKJENT_VEILEDER);
    }

    @EventListener
    public void avtaleEndret(AvtaleEndret event) {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(), VarslbarHendelseType.ENDRET, service, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        service.opprettNyBeskjed(
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
                        service,
                        parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        service.opprettOppgave(
                notifikasjon,
                NotifikasjonMerkelapp.TILTAK,
                NotifikasjonTekst.TILTAK_AVTALE_INNGATT
        );
    }

    @EventListener
    public void avbrutt(AvbruttAvVeileder event) {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(), VarslbarHendelseType.AVBRUTT, service, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        service.opprettNyBeskjed(
                notifikasjon,
                NotifikasjonMerkelapp.TILTAK,
                NotifikasjonTekst.TILTAK_AVTALE_AVBRUTT
        );
    }

    @EventListener
    public void låstOpp(AvtaleLåstOpp event) {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(), VarslbarHendelseType.LÅST_OPP, service, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        service.opprettOppgave(
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
                            service,
                            parser);
            arbeidsgiverNotifikasjonRepository.save(notifikasjon);
            service.opprettOppgave(
                    notifikasjon,
                    NotifikasjonMerkelapp.TILTAK,
                    NotifikasjonTekst.TILTAK_AVTALE_GJENOPPRETTET
            );
    }
}
