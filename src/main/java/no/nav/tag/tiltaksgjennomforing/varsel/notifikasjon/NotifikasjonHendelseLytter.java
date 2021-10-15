package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.events.*;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelseType;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.MutationStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@ConditionalOnProperty("tiltaksgjennomforing.notifikasjoner.enabled")
public class NotifikasjonHendelseLytter {

    private final ArbeidsgiverNotifikasjonRepository arbeidsgiverNotifikasjonRepository;
    private final NotifikasjonService notifikasjonService;
    private final NotifikasjonParser parser;

    private void opprettOgSendNyBeskjed(Avtale avtale, VarslbarHendelseType hendelseType, NotifikasjonTekst tekst) {
        final ArbeidsgiverNotifikasjon notifikasjon = ArbeidsgiverNotifikasjon.nyHendelse(avtale,
                hendelseType, notifikasjonService, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonService.opprettNyBeskjed(notifikasjon,
                NotifikasjonMerkelapp.getMerkelapp(avtale.getTiltakstype().getBeskrivelse()),
                tekst);
    }

    @EventListener
    public void avtaleOpprettet(AvtaleOpprettetAvVeileder event) {
        final ArbeidsgiverNotifikasjon notifikasjon = ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(),
                VarslbarHendelseType.OPPRETTET, notifikasjonService, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonService.opprettOppgave(notifikasjon,
                NotifikasjonMerkelapp.getMerkelapp(event.getAvtale().getTiltakstype().getBeskrivelse()),
                NotifikasjonTekst.AVTALE_OPPRETTET);
    }

    @EventListener
    public void avtaleKlarForRefusjon(RefusjonKlar event) {
        opprettOgSendNyBeskjed(event.getAvtale(), VarslbarHendelseType.REFUSJON_KLAR,
                NotifikasjonTekst.TILTAK_AVTALE_KLAR_REFUSJON);
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
        opprettOgSendNyBeskjed(event.getAvtale(), VarslbarHendelseType.GODKJENT_AV_VEILEDER,
                NotifikasjonTekst.TILTAK_AVTALE_GODKJENT_VEILEDER);
        notifikasjonService.oppgaveUtfoert(event.getAvtale().getId(),
                VarslbarHendelseType.OPPRETTET, MutationStatus.NY_OPPGAVE_VELLYKKET);
    }

    @EventListener
    public void avtaleInngått(AvtaleInngått event) {
        final ArbeidsgiverNotifikasjon notifikasjon = ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(),
                VarslbarHendelseType.AVTALE_INNGÅTT, notifikasjonService, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonService.opprettOppgave(notifikasjon,
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

    @EventListener
    public void endreStillingbeskrivelse(StillingsbeskrivelseEndret event) {
        opprettOgSendNyBeskjed(event.getAvtale(), VarslbarHendelseType.STILLINGSBESKRIVELSE_ENDRET,
                NotifikasjonTekst.TILTAK_STILLINGSBESKRIVELSE_ENDRET);
    }

    @EventListener
    public void endreOppfølgingOgTilretteleggingInformasjon(OppfølgingOgTilretteleggingEndret event) {
        opprettOgSendNyBeskjed(event.getAvtale(), VarslbarHendelseType.OPPFØLGING_OG_TILRETTELEGGING_ENDRET,
                NotifikasjonTekst.TILTAK_OPPFØLGING_OG_TILRETTELEGGING_ENDRET);
    }

    @EventListener
    public void endreKontaktInformasjon(KontaktinformasjonEndret event) {
        opprettOgSendNyBeskjed(event.getAvtale(), VarslbarHendelseType.KONTAKTINFORMASJON_ENDRET,
                NotifikasjonTekst.TILTAK_KONTAKTINFORMASJON_ENDRET);
    }

    @EventListener
    public void endreTilskuddsberegning(TilskuddsberegningEndret event) {
        opprettOgSendNyBeskjed(event.getAvtale(), VarslbarHendelseType.TILSKUDDSBEREGNING_ENDRET,
                NotifikasjonTekst.TILTAK_TILSKUDDSBEREGNING_ENDRET);
    }

    @EventListener
    public void forkortAvtale(AvtaleForkortet event) {
        opprettOgSendNyBeskjed(event.getAvtale(), VarslbarHendelseType.AVTALE_FORKORTET,
                NotifikasjonTekst.TILTAK_AVTALE_FORKORTET);
    }

    @EventListener
    public void forlengAvtale(AvtaleForlenget event) {
        opprettOgSendNyBeskjed(event.getAvtale(), VarslbarHendelseType.AVTALE_FORLENGET,
                NotifikasjonTekst.TILTAK_AVTALE_FORLENGET);
    }
}
