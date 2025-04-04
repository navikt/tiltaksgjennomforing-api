package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AnnullertAvSystem;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AnnullertAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForkortetAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForlengetAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForlengetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleInngått;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleSlettemerket;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjenningerOpphevetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentPaVegneAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentPaVegneAvDeltaker;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentPaVegneAvDeltakerOgArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.InkluderingstilskuddEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.KontaktinformasjonEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.MålEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.OmMentorEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.OppfølgingOgTilretteleggingEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.RefusjonKlar;
import no.nav.tag.tiltaksgjennomforing.avtale.events.StillingsbeskrivelseEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsberegningEndret;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.MutationStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("tiltaksgjennomforing.notifikasjoner.enabled")
@Slf4j
public class NotifikasjonHendelseLytter {

    private final ArbeidsgiverNotifikasjonRepository arbeidsgiverNotifikasjonRepository;
    private final NotifikasjonService notifikasjonService;
    private final NotifikasjonParser parser;
    private final FeatureToggleService featureToggleService;

    private void opprettOgSendNyBeskjed(Avtale avtale, HendelseType hendelseType, NotifikasjonTekst tekst) {
        final ArbeidsgiverNotifikasjon notifikasjon = ArbeidsgiverNotifikasjon.nyHendelse(avtale,
                hendelseType, notifikasjonService, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonService.opprettNyBeskjed(notifikasjon,
                NotifikasjonMerkelapp.getMerkelapp(avtale.getTiltakstype().getBeskrivelse()),
                tekst);
    }

    @EventListener
    public void avtaleOpprettet(AvtaleOpprettetAvVeileder event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return;
        final ArbeidsgiverNotifikasjon notifikasjon = ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(),
                HendelseType.OPPRETTET, notifikasjonService, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonService.opprettOppgave(notifikasjon,
                NotifikasjonMerkelapp.getMerkelapp(event.getAvtale().getTiltakstype().getBeskrivelse()),
                NotifikasjonTekst.TILTAK_AVTALE_OPPRETTET);
    }

    @EventListener
    public void godkjenningerOpphevetAvVeileder(GodkjenningerOpphevetAvVeileder event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return;
        final ArbeidsgiverNotifikasjon notifikasjon = ArbeidsgiverNotifikasjon.nyHendelse(event.getAvtale(),
                HendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER, notifikasjonService, parser);
        arbeidsgiverNotifikasjonRepository.save(notifikasjon);
        notifikasjonService.opprettOppgave(notifikasjon,
                NotifikasjonMerkelapp.getMerkelapp(event.getAvtale().getTiltakstype().getBeskrivelse()),
                NotifikasjonTekst.TILTAK_GODKJENNINGER_OPPHEVET_AV_VEILEDER);
    }

    @EventListener
    public void avtaleKlarForRefusjon(RefusjonKlar event) {
        opprettOgSendNyBeskjed(event.getAvtale(), HendelseType.REFUSJON_KLAR,
                NotifikasjonTekst.TILTAK_AVTALE_KLAR_REFUSJON);
    }

    @EventListener
    public void godkjentAvArbeidsgiver(GodkjentAvArbeidsgiver event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return; // notifikasjon henter alle notifikasjoner på avtalen og fullfører de
        notifikasjonService.oppgaveUtfoert(
                event.getAvtale(), HendelseType.OPPRETTET,
                MutationStatus.NY_OPPGAVE_VELLYKKET, HendelseType.AVTALE_INNGÅTT);
        notifikasjonService.oppgaveUtfoert(event.getAvtale(),
                HendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER, MutationStatus.NY_OPPGAVE_VELLYKKET, HendelseType.AVTALE_INNGÅTT);
    }

    @EventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return; // notifikasjon henter alle notifikasjoner på avtalen og fullfører de
        notifikasjonService.oppgaveUtfoert(event.getAvtale(),
                HendelseType.OPPRETTET, MutationStatus.NY_OPPGAVE_VELLYKKET, HendelseType.AVTALE_INNGÅTT);
        notifikasjonService.oppgaveUtfoert(event.getAvtale(),
                HendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER, MutationStatus.NY_OPPGAVE_VELLYKKET, HendelseType.AVTALE_INNGÅTT);
    }

    @EventListener
    public void godkjentPaVegneAv(GodkjentPaVegneAvDeltaker event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return; // notifikasjon henter alle notifikasjoner på avtalen og fullfører de
        notifikasjonService.oppgaveUtfoert(event.getAvtale(),
                HendelseType.OPPRETTET, MutationStatus.NY_OPPGAVE_VELLYKKET, HendelseType.AVTALE_INNGÅTT);
        notifikasjonService.oppgaveUtfoert(event.getAvtale(),
                HendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER, MutationStatus.NY_OPPGAVE_VELLYKKET, HendelseType.AVTALE_INNGÅTT);
    }

    @EventListener
    public void godkjentPaVegneAvArbeidsgiver(GodkjentPaVegneAvArbeidsgiver event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return; // notifikasjon henter alle notifikasjoner på avtalen og fullfører de
        notifikasjonService.oppgaveUtfoert(event.getAvtale(),
                HendelseType.OPPRETTET, MutationStatus.NY_OPPGAVE_VELLYKKET, HendelseType.AVTALE_INNGÅTT);
        notifikasjonService.oppgaveUtfoert(event.getAvtale(),
                HendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER, MutationStatus.NY_OPPGAVE_VELLYKKET, HendelseType.AVTALE_INNGÅTT);
    }

    @EventListener
    public void godkjentPaVegneAvDeltakerOgArbeidsgiver(GodkjentPaVegneAvDeltakerOgArbeidsgiver event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return; // notifikasjon henter alle notifikasjoner på avtalen og fullfører de
        notifikasjonService.oppgaveUtfoert(event.getAvtale(),
                HendelseType.OPPRETTET, MutationStatus.NY_OPPGAVE_VELLYKKET, HendelseType.AVTALE_INNGÅTT);
        notifikasjonService.oppgaveUtfoert(event.getAvtale(),
                HendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER, MutationStatus.NY_OPPGAVE_VELLYKKET, HendelseType.AVTALE_INNGÅTT);
    }

    @EventListener
    public void avtaleInngått(AvtaleInngått event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return; // notifikasjon henter alle notifikasjoner på avtalen og fullfører de
        notifikasjonService.oppgaveUtfoert(event.getAvtale(),
                HendelseType.OPPRETTET, MutationStatus.NY_OPPGAVE_VELLYKKET, HendelseType.AVTALE_INNGÅTT);
        notifikasjonService.oppgaveUtfoert(event.getAvtale(),
                HendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER, MutationStatus.NY_OPPGAVE_VELLYKKET, HendelseType.AVTALE_INNGÅTT);
        opprettOgSendNyBeskjed(event.getAvtale(), HendelseType.AVTALE_INNGÅTT, NotifikasjonTekst.TILTAK_AVTALE_INNGATT);
    }

    @EventListener
    public void sletteNotifikasjon(AvtaleSlettemerket event) {
        notifikasjonService.softDeleteNotifikasjoner(event.getAvtale());
    }

    @EventListener
    public void avtaleAnnullertAvVeileder(AnnullertAvVeileder event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return; // notifikasjon henter alle notifikasjoner på avtalen og softdeleter de
        notifikasjonService.softDeleteNotifikasjoner(event.getAvtale());
    }

    @EventListener
    public void avtaleAnnullertAvSystem(AnnullertAvSystem event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return; // notifikasjon henter alle notifikasjoner på avtalen og softdeleter de
        notifikasjonService.softDeleteNotifikasjoner(event.getAvtale());
    }

    @EventListener
    public void målEndret(MålEndret event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return;
        opprettOgSendNyBeskjed(event.getAvtale(), HendelseType.MÅL_ENDRET,
                NotifikasjonTekst.TILTAK_MÅL_ENDRET);
    }

    @EventListener
    public void inkluderingstilskuddEndret(InkluderingstilskuddEndret event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return;
        opprettOgSendNyBeskjed(event.getAvtale(), HendelseType.INKLUDERINGSTILSKUDD_ENDRET,
                NotifikasjonTekst.TILTAK_INKLUDERINGSTILSKUDD_ENDRET);
    }

    @EventListener
    public void omMentorEndret(OmMentorEndret event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return;
        opprettOgSendNyBeskjed(event.getAvtale(), HendelseType.OM_MENTOR_ENDRET,
                NotifikasjonTekst.TILTAK_OM_MENTOR_ENDRET);
    }

    @EventListener
    public void endreStillingbeskrivelse(StillingsbeskrivelseEndret event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return;
        opprettOgSendNyBeskjed(event.getAvtale(), HendelseType.STILLINGSBESKRIVELSE_ENDRET,
                NotifikasjonTekst.TILTAK_STILLINGSBESKRIVELSE_ENDRET);
    }

    @EventListener
    public void endreOppfølgingOgTilretteleggingInformasjon(OppfølgingOgTilretteleggingEndret event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return;
        opprettOgSendNyBeskjed(event.getAvtale(), HendelseType.OPPFØLGING_OG_TILRETTELEGGING_ENDRET,
                NotifikasjonTekst.TILTAK_OPPFØLGING_OG_TILRETTELEGGING_ENDRET);
    }

    @EventListener
    public void endreKontaktInformasjon(KontaktinformasjonEndret event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return;
        opprettOgSendNyBeskjed(event.getAvtale(), HendelseType.KONTAKTINFORMASJON_ENDRET,
                NotifikasjonTekst.TILTAK_KONTAKTINFORMASJON_ENDRET);
    }

    @EventListener
    public void endreTilskuddsberegning(TilskuddsberegningEndret event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return;
        opprettOgSendNyBeskjed(event.getAvtale(), HendelseType.TILSKUDDSBEREGNING_ENDRET,
                NotifikasjonTekst.TILTAK_TILSKUDDSBEREGNING_ENDRET);
    }

    @EventListener
    public void forlengetAvtaleAvVeileder(AvtaleForlengetAvVeileder event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return;
        opprettOgSendNyBeskjed(event.getAvtale(), HendelseType.AVTALE_FORLENGET,
                NotifikasjonTekst.TILTAK_AVTALE_FORLENGET);
    }

    @EventListener
    public void forlengetAvtaleAvArena(AvtaleForlengetAvArena event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return;
        opprettOgSendNyBeskjed(event.getAvtale(), HendelseType.AVTALE_FORLENGET_AV_ARENA,
            NotifikasjonTekst.TILTAK_AVTALE_FORLENGET);
    }

    @EventListener
    public void forkortAvtaleAvVeileder(AvtaleForlengetAvVeileder event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return;
        opprettOgSendNyBeskjed(event.getAvtale(), HendelseType.AVTALE_FORKORTET,
            NotifikasjonTekst.TILTAK_AVTALE_FORKORTET);
    }

    @EventListener
    public void forkortAvtaleAvArena(AvtaleForkortetAvArena event) {
        if (smsMinSideArbeidsgiverToggleErPå()) return;
        opprettOgSendNyBeskjed(event.getAvtale(), HendelseType.AVTALE_FORKORTET_AV_ARENA,
            NotifikasjonTekst.TILTAK_AVTALE_FORKORTET);
    }

    private boolean smsMinSideArbeidsgiverToggleErPå() {
        Boolean smsMinSideArbeidsgiverTogglePå = featureToggleService.isEnabled(FeatureToggle.ARBEIDSGIVERNOTIFIKASJON_MED_SAK_OG_SMS);
        if (smsMinSideArbeidsgiverTogglePå) {
            log.info("Toggle arbeidsgivernotifikasjon-med-sak-og-sms er PÅ: oppretter ikke notifikasjon til arbeidsgiver");
            return true;
        } else {
            log.info("Toggle arbeidsgivernotifikasjon-med-sak-og-sms er AV: oppretter notifikasjon til arbeidsgiver som vanlig");
            return false;
        }
    }
}
