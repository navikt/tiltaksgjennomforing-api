package no.nav.tag.tiltaksgjennomforing.hendelselogg;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.events.*;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelseType;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LyttPåHendelseTilHendelselogg {
    private final HendelseloggRepository repository;

    @EventListener
    public void avtaleOpprettetAvVeileder(AvtaleOpprettetAvVeileder event) {
        Hendelselogg hendelselogg = Hendelselogg.nyHendelse(event.getAvtale().getId(), Avtalerolle.VEILEDER, VarslbarHendelseType.OPPRETTET);
        repository.save(hendelselogg);
    }

    @EventListener
    public void avtaleOpprettetAvArbeidsgiver(AvtaleOpprettetAvArbeidsgiver event) {
        Hendelselogg hendelselogg = Hendelselogg.nyHendelse(event.getAvtale().getId(), Avtalerolle.ARBEIDSGIVER, VarslbarHendelseType.OPPRETTET_AV_ARBEIDSGIVER);
        repository.save(hendelselogg);
    }

    @EventListener
    public void avtaleEndret(AvtaleEndret event) {
        Hendelselogg hendelselogg = Hendelselogg.nyHendelse(event.getAvtale().getId(), event.getUtfortAv(), VarslbarHendelseType.ENDRET);
        repository.save(hendelselogg);
    }

    @EventListener
    public void godkjentAvDeltaker(GodkjentAvDeltaker event) {
        Hendelselogg hendelselogg = Hendelselogg.nyHendelse(event.getAvtale().getId(), Avtalerolle.DELTAKER, VarslbarHendelseType.GODKJENT_AV_DELTAKER);
        repository.save(hendelselogg);
    }

    @EventListener
    public void godkjentAvArbeidsgiver(GodkjentAvArbeidsgiver event) {
        Hendelselogg hendelselogg = Hendelselogg.nyHendelse(event.getAvtale().getId(), Avtalerolle.ARBEIDSGIVER, VarslbarHendelseType.GODKJENT_AV_ARBEIDSGIVER);
        repository.save(hendelselogg);
    }

    @EventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) {
        Hendelselogg hendelselogg = Hendelselogg.nyHendelse(event.getAvtale().getId(), Avtalerolle.VEILEDER, VarslbarHendelseType.GODKJENT_AV_VEILEDER);
        repository.save(hendelselogg);
    }

    @EventListener
    public void godkjentPaVegneAv(GodkjentPaVegneAv event) {
        Hendelselogg hendelselogg = Hendelselogg.nyHendelse(event.getAvtale().getId(), Avtalerolle.VEILEDER, VarslbarHendelseType.GODKJENT_PAA_VEGNE_AV);
        repository.save(hendelselogg);
    }

    @EventListener
    public void godkjenningerOpphevetAvArbeidsgiver(GodkjenningerOpphevetAvArbeidsgiver event) {
        Hendelselogg hendelselogg = Hendelselogg.nyHendelse(event.getAvtale().getId(), Avtalerolle.ARBEIDSGIVER, VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER);
        repository.save(hendelselogg);
    }

    @EventListener
    public void godkjenningerOpphevetAvVeileder(GodkjenningerOpphevetAvVeileder event) {
        Hendelselogg hendelselogg = Hendelselogg.nyHendelse(event.getAvtale().getId(), Avtalerolle.VEILEDER, VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER);
        repository.save(hendelselogg);
    }

    @EventListener
    public void avtaleLåstOpp(AvtaleLåstOpp event) {
        Hendelselogg hendelselogg = Hendelselogg.nyHendelse(event.getAvtale().getId(), Avtalerolle.VEILEDER, VarslbarHendelseType.LÅST_OPP);
        repository.save(hendelselogg);
    }

    @EventListener
    public void avbruttAvVeileder(AvbruttAvVeileder event) {
        Hendelselogg hendelselogg = Hendelselogg.nyHendelse(event.getAvtale().getId(), Avtalerolle.VEILEDER, VarslbarHendelseType.AVBRUTT);
        repository.save(hendelselogg);
    }

    @EventListener
    public void gjenopprettet(AvtaleGjenopprettet event) {
        Hendelselogg hendelselogg = Hendelselogg.nyHendelse(event.getAvtale().getId(), Avtalerolle.VEILEDER, VarslbarHendelseType.GJENOPPRETTET);
        repository.save(hendelselogg);
    }

    @EventListener
    public void avtaleEndretVeileder(AvtaleNyVeileder event) {
        Hendelselogg hendelselogg = Hendelselogg.nyHendelse(event.getAvtale().getId(), Avtalerolle.VEILEDER, VarslbarHendelseType.NY_VEILEDER);
        repository.save(hendelselogg);
    }

    @EventListener
    public void ufordeltAvtaleTildeltVeileder(AvtaleOpprettetAvArbeidsgiverErFordelt event) {
        Hendelselogg hendelselogg = Hendelselogg.nyHendelse(event.getAvtale().getId(), Avtalerolle.VEILEDER, VarslbarHendelseType.AVTALE_FORDELT);
        repository.save(hendelselogg);
    }
}
