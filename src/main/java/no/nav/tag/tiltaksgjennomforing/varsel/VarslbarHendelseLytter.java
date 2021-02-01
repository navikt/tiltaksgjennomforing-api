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
    public void godkjentPaVegneAv(GodkjentPaVegneAv event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENT_PAA_VEGNE_AV));
    }
}
