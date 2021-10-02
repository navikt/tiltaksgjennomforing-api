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
    NotifikasjonMSAService notifikasjonMSAService;

    @Autowired
    NotifikasjonParser notifikasjonParser;

    @EventListener
    public void avtaleOpprettet(AvtaleOpprettetAvVeileder event) {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(
                        event.getAvtale(),
                        VarslbarHendelseType.OPPRETTET,
                        notifikasjonMSAService,
                        notifikasjonParser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonMSAService.opprettNyBeskjed(
                notifikasjon,
                NotifikasjonMerkelapp.TILTAK,
                NotifikasjonTekst.AVTALE_OPPRETTET);
    }

    @EventListener
    public void avtaleKlarForRefusjon(RefusjonKlar event) {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(),
                        VarslbarHendelseType.REFUSJON_KLAR,
                        notifikasjonMSAService,
                        notifikasjonParser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonMSAService.opprettOppgave(
                notifikasjon,
                NotifikasjonMerkelapp.REFUSJON,
                NotifikasjonTekst.TILTAK_AVTALE_KLAR_REFUSJON
        );
    }

    @EventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(),
                        VarslbarHendelseType.GODKJENT_AV_VEILEDER,
                        notifikasjonMSAService,
                        notifikasjonParser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonMSAService.opprettNyBeskjed(
                notifikasjon,
                NotifikasjonMerkelapp.TILTAK,
                NotifikasjonTekst.TILTAK_AVTALE_GODKJENT_VEILEDER);
    }

    @EventListener
    public void avtaleEndret(AvtaleEndret event) {
        final ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(),
                        VarslbarHendelseType.ENDRET,
                        notifikasjonMSAService,
                        notifikasjonParser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonMSAService.opprettOppgave(
                notifikasjon,
                NotifikasjonMerkelapp.TILTAK,
                NotifikasjonTekst.TILTAK_AVTALE_ENDRET
        );
    }

    @EventListener
    public void avtaleInngått(AvtaleInngått event) {
        final  ArbeidsgiverNotifikasjon notifikasjon =
                ArbeidsgiverNotifikasjon.nyHendelse(
                        event.getAvtale(),
                        VarslbarHendelseType.AVTALE_INNGÅTT,
                        notifikasjonMSAService,
                        notifikasjonParser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonMSAService.opprettNyBeskjed(
                notifikasjon,
                NotifikasjonMerkelapp.TILTAK,
                NotifikasjonTekst.TILTAK_AVTALE_INNGATT
        );
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
