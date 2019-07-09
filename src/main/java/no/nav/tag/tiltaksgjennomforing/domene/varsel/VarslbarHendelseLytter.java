package no.nav.tag.tiltaksgjennomforing.domene.varsel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.domene.events.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class VarslbarHendelseLytter {
    private final VarslbarHendelseRepository varslbarHendelseRepository;

    @EventListener
    public void avtaleOpprettet(AvtaleOpprettet event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.OPPRETTET));
    }

    @EventListener
    public void avtaleEndret(AvtaleEndret event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.ENDRET));
    }

    @EventListener
    public void godkjenningerOpphevetAvArbeidsgiver(GodkjenningerOpphevetAvArbeidsgiver event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER));
    }

    @EventListener
    public void godkjenningerOpphevetAvVeileder(GodkjenningerOpphevetAvVeileder event) {
        varslbarHendelseRepository.save(VarslbarHendelse.nyHendelse(event.getAvtale(), VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER));
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
