package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.events.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VarslbarHendelseLytter {
    private final VarslbarHendelseRepository varslbarHendelseRepository;

    @EventListener
    public void avtaleOpprettet(AvtaleOpprettetAvVeileder event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.OPPRETTET));
    }

    @EventListener
    public void avtaleKlarForRefusjon(RefusjonKlar event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.REFUSJON_KLAR));
    }
    @EventListener
    public void avtaleKlarForRefusjonRevarsel(RefusjonKlarRevarsel event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.REFUSJON_KLAR_REVARSEL));
    }
    @EventListener
    public void refusjonFristForlengetVarsel(RefusjonFristForlenget event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.REFUSJON_FRIST_FORLENGET));
    }

    @EventListener
    public void avtaleOpprettetAvArbeidsgiver(AvtaleOpprettetAvArbeidsgiver event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.OPPRETTET_AV_ARBEIDSGIVER));
    }

    @EventListener
    public void avtaleDeltMedAvtalepart(AvtaleDeltMedAvtalepart event) {
        if (event.getAvtalepart() == Avtalerolle.ARBEIDSGIVER) {
            varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.DELT_MED_ARBEIDSGIVER));
        } else if (event.getAvtalepart() == Avtalerolle.DELTAKER) {
            varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.DELT_MED_DELTAKER));
        }
    }

    @EventListener
    public void tilskuddsperiodeAvslått(TilskuddsperiodeAvslått event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.TILSKUDDSPERIODE_AVSLATT));
    }

    @EventListener
    public void tilskuddsperiodeGodkjent(TilskuddsperiodeGodkjent event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.TILSKUDDSPERIODE_GODKJENT));
    }

    @EventListener
    public void avtaleEndret(AvtaleEndret event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.ENDRET));
    }

    @EventListener
    public void godkjenningerOpphevetAvArbeidsgiver(GodkjenningerOpphevetAvArbeidsgiver event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER, event.getGamleVerdier()));
    }

    @EventListener
    public void godkjenningerOpphevetAvVeileder(GodkjenningerOpphevetAvVeileder event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER, event.getGamleVerdier()));
    }

    @EventListener
    public void godkjentAvDeltaker(GodkjentAvDeltaker event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENT_AV_DELTAKER));
    }

    @EventListener
    public void godkjentAvArbeidsgiver(GodkjentAvArbeidsgiver event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENT_AV_ARBEIDSGIVER));
    }

    @EventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENT_AV_VEILEDER));
    }

    @EventListener
    public void avtaleInngått(AvtaleInngått event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.AVTALE_INNGÅTT));
    }

    @EventListener
    public void godkjentPaVegneAv(GodkjentPaVegneAv event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENT_PAA_VEGNE_AV));
    }

    @EventListener
    public void godkjentPaVegneAvArbeidsgiver(GodkjentPaVegneAvArbeidsgiver event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENT_PAA_VEGNE_AV_ARBEIDSGIVER));
    }

    @EventListener
    public void godkjentPaVegneAvDeltakerOgArbeidsgiver(GodkjentPaVegneAvDeltakerOgArbeidsgiver event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENT_PAA_VEGNE_AV_DELTAKER_OG_ARBEIDSGIVER));
    }

    @EventListener
    public void nyVeileder(AvtaleNyVeileder event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.NY_VEILEDER));
    }

    @EventListener
    public void fordelt(AvtaleOpprettetAvArbeidsgiverErFordelt event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.AVTALE_FORDELT));
    }

    @EventListener
    public void avbrutt(AvbruttAvVeileder event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.AVBRUTT));
    }

    @EventListener
    public void låstOpp(AvtaleLåstOpp event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.LÅST_OPP));
    }

    @EventListener
    public void gjenopprettet(AvtaleGjenopprettet event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.GJENOPPRETTET));
    }
}
