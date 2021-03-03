package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.events.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LagVarselFraAvtaleHendelser {
    private final VarselRepository varselRepository;

    @EventListener
    public void avtaleOpprettet(AvtaleOpprettetAvVeileder event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.VEILEDER, VarslbarHendelseType.OPPRETTET);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void avtaleOpprettetAvArbeidsgiver(AvtaleOpprettetAvArbeidsgiver event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.ARBEIDSGIVER, VarslbarHendelseType.OPPRETTET_AV_ARBEIDSGIVER);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void avtaleDeltMedAvtalepart(AvtaleDeltMedAvtalepart event) {
        if (event.getAvtalepart() == Avtalerolle.ARBEIDSGIVER) {
            VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.VEILEDER, VarslbarHendelseType.DELT_MED_ARBEIDSGIVER);
            varselRepository.saveAll(List.of(factory.veileder(), factory.arbeidsgiver()));
        } else if (event.getAvtalepart() == Avtalerolle.DELTAKER) {
            VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.VEILEDER, VarslbarHendelseType.DELT_MED_DELTAKER);
            varselRepository.saveAll(List.of(factory.veileder(), factory.deltaker()));
        }
    }

    @EventListener
    public void tilskuddsperiodeAvslått(TilskuddsperiodeAvslått event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.BESLUTTER, VarslbarHendelseType.TILSKUDDSPERIODE_AVSLATT);
        varselRepository.saveAll(List.of(factory.veileder()));
    }

    @EventListener
    public void tilskuddsperiodeGodkjent(TilskuddsperiodeGodkjent event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.BESLUTTER, VarslbarHendelseType.TILSKUDDSPERIODE_GODKJENT);
        varselRepository.saveAll(List.of(factory.veileder()));
    }

    @EventListener
    public void avtaleEndret(AvtaleEndret event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), event.getUtfortAv(), VarslbarHendelseType.ENDRET);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void godkjenningerOpphevetAvArbeidsgiver(GodkjenningerOpphevetAvArbeidsgiver event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.ARBEIDSGIVER, VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void godkjenningerOpphevetAvVeileder(GodkjenningerOpphevetAvVeileder event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.VEILEDER, VarslbarHendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void godkjentAvDeltaker(GodkjentAvDeltaker event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.DELTAKER, VarslbarHendelseType.GODKJENT_AV_DELTAKER);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void godkjentAvArbeidsgiver(GodkjentAvArbeidsgiver event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.ARBEIDSGIVER, VarslbarHendelseType.GODKJENT_AV_ARBEIDSGIVER);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.VEILEDER, VarslbarHendelseType.GODKJENT_AV_VEILEDER);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void godkjentPaVegneAv(GodkjentPaVegneAv event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.VEILEDER, VarslbarHendelseType.GODKJENT_PAA_VEGNE_AV);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void nyVeileder(AvtaleNyVeileder event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.VEILEDER, VarslbarHendelseType.NY_VEILEDER);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void fordelt(AvtaleOpprettetAvArbeidsgiverErFordelt event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.VEILEDER, VarslbarHendelseType.AVTALE_FORDELT);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void avbrutt(AvbruttAvVeileder event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.VEILEDER, VarslbarHendelseType.AVBRUTT);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void låstOpp(AvtaleLåstOpp event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.VEILEDER, VarslbarHendelseType.LÅST_OPP);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void gjenopprettet(AvtaleGjenopprettet event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.VEILEDER, VarslbarHendelseType.GJENOPPRETTET);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void forlengAvtale(AvtaleForlenget event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.VEILEDER, VarslbarHendelseType.AVTALE_FORLENGET);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void endreTilskuddsberegning(TilskuddsberegningEndret event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), Avtalerolle.VEILEDER, VarslbarHendelseType.TILSKUDDSBEREGNING_ENDRET);
        varselRepository.saveAll(factory.alleParter());
    }
}
