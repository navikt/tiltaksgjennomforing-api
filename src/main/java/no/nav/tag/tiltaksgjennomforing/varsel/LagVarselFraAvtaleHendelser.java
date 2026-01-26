package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.HendelseType;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AnnullertAvSystem;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AnnullertAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleDeltMedAvtalepart;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleEndretAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleFordelt;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForkortetAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForkortetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForlengetAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleForlengetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleGjenopprettet;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleInngått;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleLåstOpp;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleNyVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvArena;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleOpprettetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.AvtaleUtloperVarsel;
import no.nav.tag.tiltaksgjennomforing.avtale.events.FjernetEtterregistrering;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjenningerOpphevetAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjenningerOpphevetAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentAvDeltaker;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentForEtterregistrering;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentPaVegneAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentPaVegneAvDeltaker;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentPaVegneAvDeltakerOgArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.InkluderingstilskuddEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.KidOgKontonummerEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.KontaktinformasjonEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.MålEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.OmMentorEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.OppfolgingAvAvtaleGodkjent;
import no.nav.tag.tiltaksgjennomforing.avtale.events.OppfølgingOgTilretteleggingEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.SignertAvMentor;
import no.nav.tag.tiltaksgjennomforing.avtale.events.StillingsbeskrivelseEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsberegningEndret;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsperiodeAvslått;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsperiodeGodkjent;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleHendelseUtførtAv;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LagVarselFraAvtaleHendelser {
    private final VarselRepository varselRepository;

    @EventListener
    public void avtaleOpprettet(AvtaleOpprettetAvVeileder event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getUtfortAv()), HendelseType.OPPRETTET);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void avtaleOpprettetAvArbeidsgiver(AvtaleOpprettetAvArbeidsgiver event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.arbeidsgiver(event.getAvtale()), HendelseType.OPPRETTET_AV_ARBEIDSGIVER);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void avtaleOpprettetAvArena(AvtaleOpprettetAvArena event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.system(Identifikator.ARENA), HendelseType.OPPRETTET_AV_ARENA);
        varselRepository.save(factory.veileder());
    }

    @EventListener
    public void avtaleDeltMedAvtalepart(AvtaleDeltMedAvtalepart event) {
        if (event.getAvtalepart() == Avtalerolle.ARBEIDSGIVER) {
            VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(null), HendelseType.DELT_MED_ARBEIDSGIVER);
            varselRepository.saveAll(List.of(factory.veileder(), factory.arbeidsgiver()));
        } else if (event.getAvtalepart() == Avtalerolle.DELTAKER) {
            VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(null), HendelseType.DELT_MED_DELTAKER);
            varselRepository.saveAll(List.of(factory.veileder(), factory.deltaker()));
        } else if (event.getAvtalepart() == Avtalerolle.MENTOR) {
            VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(null), HendelseType.DELT_MED_MENTOR);
            varselRepository.saveAll(List.of(factory.veileder(), factory.mentor()));
        }
    }

    //TODO: Hent IDENTEN til beslutter her og ikke veileder
    @EventListener
    public void tilskuddsperiodeAvslått(TilskuddsperiodeAvslått event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), event.getTilskuddsperiode(), AvtaleHendelseUtførtAv.beslutter(event.getUtfortAv()), HendelseType.TILSKUDDSPERIODE_AVSLATT);
        varselRepository.saveAll(List.of(factory.veileder()));
    }

    //TODO: Hent IDENTEN til beslutter her og ikke veileder
    @EventListener
    public void tilskuddsperiodeGodkjent(TilskuddsperiodeGodkjent event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), event.getTilskuddsperiode(), AvtaleHendelseUtførtAv.beslutter(event.getUtfortAv()), HendelseType.TILSKUDDSPERIODE_GODKJENT);
        varselRepository.saveAll(List.of(factory.veileder()));
    }

    @EventListener
    public void avtaleEndret(AvtaleEndret event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.fra(event.getAvtale(), event.getUtfortAv(), event.getUtfortAvRolle()), HendelseType.ENDRET);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void avtaleEndret(AvtaleEndretAvArena event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.system(Identifikator.ARENA), HendelseType.ENDRET_AV_ARENA);
        varselRepository.saveAll(List.of(factory.veileder(), factory.arbeidsgiver()));
    }

    @EventListener
    public void godkjenningerOpphevetAvArbeidsgiver(GodkjenningerOpphevetAvArbeidsgiver event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.arbeidsgiver(event.getAvtale()), HendelseType.GODKJENNINGER_OPPHEVET_AV_ARBEIDSGIVER);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void godkjenningerOpphevetAvVeileder(GodkjenningerOpphevetAvVeileder event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(null), HendelseType.GODKJENNINGER_OPPHEVET_AV_VEILEDER);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void godkjentAvDeltaker(GodkjentAvDeltaker event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.deltaker(event.getUtfortAv()), HendelseType.GODKJENT_AV_DELTAKER);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void signertAvMentor(SignertAvMentor event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.mentor(event.getUtfortAv()), HendelseType.SIGNERT_AV_MENTOR);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void godkjentAvArbeidsgiver(GodkjentAvArbeidsgiver event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.arbeidsgiver(event.getAvtale()), HendelseType.GODKJENT_AV_ARBEIDSGIVER);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getUtfortAv()), HendelseType.GODKJENT_AV_VEILEDER);
        varselRepository.save(factory.veileder());
    }

    @EventListener
    public void godkjentPaVegneAv(GodkjentPaVegneAvDeltaker event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getUtfortAv()), HendelseType.GODKJENT_PAA_VEGNE_AV);
        varselRepository.save(factory.veileder());
    }

    @EventListener
    public void godkjentPaVegneAvArbeidsgiver(GodkjentPaVegneAvArbeidsgiver event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getUtfortAv()), HendelseType.GODKJENT_PAA_VEGNE_AV_ARBEIDSGIVER);
        varselRepository.save(factory.veileder());
    }

    @EventListener
    public void godkjentPaVegneAvDeltakerOgArbeidsgiver(GodkjentPaVegneAvDeltakerOgArbeidsgiver event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getUtfortAv()), HendelseType.GODKJENT_PAA_VEGNE_AV_DELTAKER_OG_ARBEIDSGIVER);
        varselRepository.save(factory.veileder());
    }

    @EventListener
    public void godkjentForEtterregistrering(GodkjentForEtterregistrering event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.beslutter(event.getUtfortAv()), HendelseType.GODKJENT_FOR_ETTERREGISTRERING);
        varselRepository.save(factory.veileder());
    }

    @EventListener
    public void fjernetEtterregistrering(FjernetEtterregistrering event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.beslutter(event.getUtfortAv()), HendelseType.FJERNET_ETTERREGISTRERING);
        varselRepository.save(factory.veileder());
    }

    @EventListener
    public void avtaleInngått(AvtaleInngått event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.fra(event.getAvtale(), event.getUtførtAv(), event.getUtførtAvRolle()), HendelseType.AVTALE_INNGÅTT);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void nyVeileder(AvtaleNyVeileder event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getAvtale().getVeilederNavIdent()), HendelseType.NY_VEILEDER);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void fordelt(AvtaleFordelt event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getAvtale().getVeilederNavIdent()), HendelseType.AVTALE_FORDELT);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void annullertAvVeileder(AnnullertAvVeileder event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getUtfortAv()), HendelseType.ANNULLERT);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void annullertAvSystem(AnnullertAvSystem event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.system(event.getUtfortAv()), HendelseType.ANNULLERT);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void låstOpp(AvtaleLåstOpp event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(null), HendelseType.LÅST_OPP);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void gjenopprettet(AvtaleGjenopprettet event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getUtfortAv()), HendelseType.GJENOPPRETTET);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void forkortAvtaleAvVeileder(AvtaleForkortetAvVeileder event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()), HendelseType.AVTALE_FORKORTET);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void forkortAvtaleAvArena(AvtaleForkortetAvArena event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.system(Identifikator.ARENA), HendelseType.AVTALE_FORKORTET_AV_ARENA);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void forlengAvtaleAvVeileder(AvtaleForlengetAvVeileder event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()), HendelseType.AVTALE_FORLENGET);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void forlengAvtaleAvArena(AvtaleForlengetAvArena event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.system(Identifikator.ARENA), HendelseType.AVTALE_FORLENGET_AV_ARENA);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void målEndret(MålEndret event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()), HendelseType.MÅL_ENDRET);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void inkluderingstilskuddEndret(InkluderingstilskuddEndret event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()), HendelseType.INKLUDERINGSTILSKUDD_ENDRET);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void omMentorEndret(OmMentorEndret event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()), HendelseType.OM_MENTOR_ENDRET);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void endreTilskuddsberegning(TilskuddsberegningEndret event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()), HendelseType.TILSKUDDSBEREGNING_ENDRET);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void endreKontaktInformasjon(KontaktinformasjonEndret event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()), HendelseType.KONTAKTINFORMASJON_ENDRET);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void endreStillingbeskrivelse(StillingsbeskrivelseEndret event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()), HendelseType.STILLINGSBESKRIVELSE_ENDRET);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void endreOppfølgingOgTilretteleggingInformasjon(OppfølgingOgTilretteleggingEndret event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()), HendelseType.OPPFØLGING_OG_TILRETTELEGGING_ENDRET);
        varselRepository.saveAll(factory.alleParter());
    }

    @EventListener
    public void endreKidOgKontonummer(KidOgKontonummerEndret event) {
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()), HendelseType.KID_OG_KONTONUMMER_ENDRET);
        varselRepository.saveAll(List.of(factory.arbeidsgiver(), factory.veileder()));
    }

    @EventListener
    public void oppfølgingAvAvtaleGodkjent(OppfolgingAvAvtaleGodkjent event) {
        Varsel veilederVarsel = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.veileder(event.getUtførtAv()), HendelseType.OPPFØLGING_AV_TILTAK_UTFØRT).veileder();
        // Veileder utfører handlingen, så vi behøver ingen varsel
        veilederVarsel.setLest(true);
        veilederVarsel.setBjelle(false);
        varselRepository.save(veilederVarsel);
    }

    @EventListener
    public void avtaleUtloperVarsel(AvtaleUtloperVarsel event) {
        HendelseType hendelseType = switch (event.getType()) {
            case OM_24_TIMER -> HendelseType.UTLOPER_OM_24_TIMER;
            case OM_EN_UKE -> HendelseType.UTLOPER_OM_1_UKE;
        };
        VarselFactory factory = new VarselFactory(event.getAvtale(), AvtaleHendelseUtførtAv.system(Identifikator.SYSTEM), hendelseType);
        varselRepository.save(factory.veileder());
    }
}
