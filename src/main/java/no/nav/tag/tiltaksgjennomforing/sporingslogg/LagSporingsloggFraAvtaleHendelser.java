package no.nav.tag.tiltaksgjennomforing.sporingslogg;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.events.*;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelseType;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LagSporingsloggFraAvtaleHendelser {
    private final SporingsloggRepository sporingsloggRepository;

    @EventListener
    public void avtaleOpprettet(AvtaleOpprettetAvVeileder event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.OPPRETTET));
    }

    @EventListener
    public void avtaleKlarForRefusjon(RefusjonKlar event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.REFUSJON_KLAR));
    }

    @EventListener
    public void avtaleKlarForRefusjonRevarsel(RefusjonKlarRevarsel event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.REFUSJON_KLAR_REVARSEL));
    }

    @EventListener
    public void refusjonFristForlengetVarsel(RefusjonFristForlenget event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.REFUSJON_FRIST_FORLENGET));
    }

    @EventListener
    public void refusjonKorrigert(RefusjonKorrigert event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.REFUSJON_KORRIGERT));
    }

    @EventListener
    public void avtaleOpprettetAvArbeidsgiver(AvtaleOpprettetAvArbeidsgiver event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.OPPRETTET_AV_ARBEIDSGIVER));
    }

    @EventListener
    public void avtaleDeltMedAvtalepart(AvtaleDeltMedAvtalepart event) {
        if (event.getAvtalepart() == Avtalerolle.ARBEIDSGIVER) {
            sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.DELT_MED_ARBEIDSGIVER));
        } else if (event.getAvtalepart() == Avtalerolle.DELTAKER) {
            sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.DELT_MED_DELTAKER));
        }
    }

    @EventListener
    public void tilskuddsperiodeAvslått(TilskuddsperiodeAvslått event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.TILSKUDDSPERIODE_AVSLATT));
    }

    @EventListener
    public void tilskuddsperiodeGodkjent(TilskuddsperiodeGodkjent event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.TILSKUDDSPERIODE_GODKJENT));
    }

    @EventListener
    public void avtaleEndret(AvtaleEndret event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.ENDRET));
    }

    @EventListener
    public void godkjenningerOpphevetAvArbeidsgiver(GodkjenningerOpphevetAvArbeidsgiver event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER));
    }

    @EventListener
    public void godkjenningerOpphevetAvVeileder(GodkjenningerOpphevetAvVeileder event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER));
    }

    @EventListener
    public void godkjentAvDeltaker(GodkjentAvDeltaker event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENT_AV_DELTAKER));
    }

    @EventListener
    public void godkjentAvArbeidsgiver(GodkjentAvArbeidsgiver event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENT_AV_ARBEIDSGIVER));
    }

    @EventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENT_AV_VEILEDER));
    }

    @EventListener
    public void avtaleInngått(AvtaleInngått event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.AVTALE_INNGÅTT));
    }

    @EventListener
    public void godkjentPaVegneAv(GodkjentPaVegneAvDeltaker event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENT_PAA_VEGNE_AV));
    }

    @EventListener
    public void godkjentPaVegneAvArbeidsgiver(GodkjentPaVegneAvArbeidsgiver event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENT_PAA_VEGNE_AV_ARBEIDSGIVER));
    }

    @EventListener
    public void godkjentPaVegneAvDeltakerOgArbeidsgiver(GodkjentPaVegneAvDeltakerOgArbeidsgiver event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENT_PAA_VEGNE_AV_DELTAKER_OG_ARBEIDSGIVER));
    }

    @EventListener
    public void nyVeileder(AvtaleNyVeileder event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.NY_VEILEDER));
    }

    @EventListener
    public void fordelt(AvtaleOpprettetAvArbeidsgiverErFordelt event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.AVTALE_FORDELT));
    }

    @EventListener
    public void avbrutt(AvbruttAvVeileder event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.AVBRUTT));
    }

    @EventListener
    public void låstOpp(AvtaleLåstOpp event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.LÅST_OPP));
    }

    @EventListener
    public void gjenopprettet(AvtaleGjenopprettet event) {
        sporingsloggRepository.save(Sporingslogg.nyHendelse(event.getAvtale(), VarslbarHendelseType.GJENOPPRETTET));
    }
}
