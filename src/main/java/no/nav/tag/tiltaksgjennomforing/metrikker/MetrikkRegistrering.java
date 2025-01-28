package no.nav.tag.tiltaksgjennomforing.metrikker;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.avtale.events.*;
import no.nav.tag.tiltaksgjennomforing.datadeling.AvtaleHendelseUtførtAvRolle;
import no.nav.tag.tiltaksgjennomforing.varsel.events.SmsSendt;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MetrikkRegistrering {
    private final MeterRegistry meterRegistry;

    @EventListener
    public void smsSendt(SmsSendt event) {
        Counter.builder("tiltaksgjennomforing.smsvarsel.sendt").register(meterRegistry).increment();
    }

    @EventListener
    public void avtaleOpprettet(AvtaleOpprettetAvVeileder event) {
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale opprettet av veileder, avtaleId={} ident={}, tiltakstype={}", event.getAvtale().getId(), event.getUtfortAv(), tiltakstype);
        counter("avtale.opprettet", AvtaleHendelseUtførtAvRolle.VEILEDER, tiltakstype).increment();
    }

    @EventListener
    public void avtaleOpprettetAvArbeidsgiver(AvtaleOpprettetAvArbeidsgiver event) {
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale opprettet av arbeidsgiver, avtaleId={}, tiltakstype={}", event.getAvtale().getId(), tiltakstype);
        counter("avtale.opprettet", AvtaleHendelseUtførtAvRolle.ARBEIDSGIVER, tiltakstype).increment();
    }

    @EventListener
    public void avtaleOpprettetAvArena(AvtaleOpprettetAvArena event) {
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale opprettet av Arena, avtaleId={}, tiltakstype={}", event.getAvtale().getId(), tiltakstype);
        counter("avtale.opprettet", AvtaleHendelseUtførtAvRolle.SYSTEM, tiltakstype).increment();
    }

    @EventListener
    public void avtaleEndret(AvtaleEndret event) {
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale endret, avtaleId={}, avtalepart={}, tiltakstype={}, opphav={}", event.getAvtale().getId(), event.getUtfortAvRolle(), tiltakstype, event.getAvtale().getOpphav());
        counter("avtale.endret", event.getUtfortAvRolle(), tiltakstype).increment();
    }

    @EventListener
    public void avtaleEndretAvArena(AvtaleEndretAvArena event) {
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale endret av Arena, avtaleId={}, tiltakstype={}", event.getAvtale().getId(), tiltakstype);
        counter("avtale.endret", AvtaleHendelseUtførtAvRolle.SYSTEM, tiltakstype).increment();
    }

    @EventListener
    public void godkjenningerOpphevet(GodkjenningerOpphevetAvVeileder event) {
        AvtaleHendelseUtførtAvRolle rolle = AvtaleHendelseUtførtAvRolle.VEILEDER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtalens godkjenninger opphevet, avtaleId={}, avtalepart={}, tiltakstype={}, opphav={}", event.getAvtale().getId(), rolle, tiltakstype, event.getAvtale().getOpphav());
        counter("avtale.godkjenning.opphevet", rolle, tiltakstype).increment();
    }

    @EventListener
    public void godkjenningerOpphevet(GodkjenningerOpphevetAvArbeidsgiver event) {
        AvtaleHendelseUtførtAvRolle rolle = AvtaleHendelseUtførtAvRolle.ARBEIDSGIVER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtalens godkjenninger opphevet, avtaleId={}, avtalepart={}, tiltakstype={}, opphav={}", event.getAvtale().getId(), rolle, tiltakstype, event.getAvtale().getOpphav());
        counter("avtale.godkjenning.opphevet", rolle, tiltakstype).increment();
    }

    @EventListener
    public void godkjentAvDeltaker(GodkjentAvDeltaker event) {
        AvtaleHendelseUtførtAvRolle rolle = AvtaleHendelseUtførtAvRolle.DELTAKER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale godkjent, avtaleId={}, avtalepart={}, tiltakstype={}, opphav={}", event.getAvtale().getId(), rolle, tiltakstype, event.getAvtale().getOpphav());
        counter("avtale.godkjenning.godkjent", rolle, tiltakstype).increment();
    }

    @EventListener
    public void godkjentAvMentor(SignertAvMentor event) {
        AvtaleHendelseUtførtAvRolle rolle = AvtaleHendelseUtførtAvRolle.MENTOR;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Mentor har signert taushetserklæring, avtaleId={}, avtalepart={}, tiltakstype={}", event.getAvtale().getId(), rolle, tiltakstype);
        counter("avtale.godkjenning.godkjent", rolle, tiltakstype).increment();
    }

    @EventListener
    public void godkjentAvArbeidsgiver(GodkjentAvArbeidsgiver event) {
        AvtaleHendelseUtførtAvRolle rolle = AvtaleHendelseUtførtAvRolle.ARBEIDSGIVER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale godkjent, avtaleId={}, avtalepart={}, tiltakstype={}, opphav={}", event.getAvtale().getId(), rolle, tiltakstype, event.getAvtale().getOpphav());
        counter("avtale.godkjenning.godkjent", rolle, tiltakstype).increment();
    }

    @EventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) {
        AvtaleHendelseUtførtAvRolle rolle = AvtaleHendelseUtførtAvRolle.VEILEDER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale godkjent, avtaleId={}, avtalepart={}, tiltakstype={}, opphav={}", event.getAvtale().getId(), rolle, tiltakstype, event.getAvtale().getOpphav());
        counter("avtale.godkjenning.godkjent", rolle, tiltakstype).increment();
    }

    @EventListener
    public void avtaleInngått(AvtaleInngått event) {
        AvtaleHendelseUtførtAvRolle rolle = event.getUtførtAvRolle();
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale inngått, avtaleId={}, avtalepart={}, tiltakstype={}, opphav={}", event.getAvtale().getId(), rolle, tiltakstype, event.getAvtale().getOpphav());
        counter("avtale.godkjenning.inngaatt", rolle, tiltakstype).increment();
    }

    @EventListener
    public void tilskuddsperiodeGodkjent(TilskuddsperiodeGodkjent event) {
        AvtaleHendelseUtførtAvRolle rolle = AvtaleHendelseUtførtAvRolle.BESLUTTER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Tilskuddsperiode godkjent, avtaleId={}, tilskuddsperiodeId={}, løpenummer={}, avtalepart={}, tiltakstype={}", event.getAvtale().getId(), event.getTilskuddsperiode().getId(), event.getTilskuddsperiode().getLøpenummer(), rolle, tiltakstype);
        counter("avtale.tilskuddsperiode.godkjent", rolle, tiltakstype).increment();
    }

    @EventListener
    public void tilskuddsperiodeAvslått(TilskuddsperiodeAvslått event) {
        AvtaleHendelseUtførtAvRolle rolle = AvtaleHendelseUtførtAvRolle.BESLUTTER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Tilskuddsperiode avslått, avtaleId={}, tilskuddsperiodeId={}, løpenummer={}, avtalepart={}, tiltakstype={}", event.getAvtale().getId(), event.getTilskuddsperiode().getId(), event.getTilskuddsperiode().getLøpenummer(), rolle, tiltakstype);
        counter("avtale.tilskuddsperiode.avslaatt", rolle, tiltakstype).increment();
    }

    @EventListener
    public void godkjentPaVegneAv(GodkjentPaVegneAvDeltaker event) {
        AvtaleHendelseUtførtAvRolle rolle = AvtaleHendelseUtførtAvRolle.VEILEDER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale godkjent på vegne av deltaker, avtaleId={}, avtalepart={}, tiltakstype={}, opphav={}", event.getAvtale().getId(), rolle, tiltakstype, event.getAvtale().getOpphav());
        counter("avtale.godkjenning.godkjentPaVegneAv", rolle, tiltakstype).increment();
    }

    @EventListener
    public void godkjentPaVegneAvArbeidsgiver(GodkjentPaVegneAvArbeidsgiver event) {
        AvtaleHendelseUtførtAvRolle rolle = AvtaleHendelseUtførtAvRolle.VEILEDER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale godkjent på vegne av arbeidsgiver, avtaleId={}, avtalepart={}, tiltakstype={}, opphav={}", event.getAvtale().getId(), rolle, tiltakstype, event.getAvtale().getOpphav());
        counter("avtale.godkjenning.godkjentPaVegneAvArbeidsgiver", rolle, tiltakstype).increment();
    }

    @EventListener
    public void godkjentPaVegneAvDeltakerOgArbeidsgiver(GodkjentPaVegneAvDeltakerOgArbeidsgiver event) {
        AvtaleHendelseUtførtAvRolle rolle = AvtaleHendelseUtførtAvRolle.VEILEDER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale godkjent på vegne av deltaker og arbeidsgiver, avtaleId={}, avtalepart={}, tiltakstype={}, opphav={}", event.getAvtale().getId(), rolle, tiltakstype, event.getAvtale().getOpphav());
        counter("avtale.godkjenning.godkjentPaVegneAvDeltakerOgArbeidsgiver", rolle, tiltakstype).increment();
    }

    @EventListener
    public void avtaleLåstOpp(AvtaleLåstOpp event) {
        AvtaleHendelseUtførtAvRolle rolle = AvtaleHendelseUtførtAvRolle.VEILEDER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale låst opp, avtaleId={}, avtalepart={}, tiltakstype={}", event.getAvtale().getId(), rolle, tiltakstype);
        counter("avtale.laastOpp", rolle, tiltakstype).increment();
    }

    @EventListener
    public void avtaleDeltMedAvtalepart(AvtaleDeltMedAvtalepart event) {
        AvtaleHendelseUtførtAvRolle rolle = AvtaleHendelseUtførtAvRolle.VEILEDER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale delt med {}, avtaleId={}, avtalepart={}, tiltakstype={}", event.getAvtalepart(), event.getAvtale().getId(), rolle, tiltakstype);
        counter("avtale.deltMedAvtalepart", rolle, tiltakstype).increment();
    }

    @EventListener
    public void avtaleAnnullertAvVeileder(AnnullertAvVeileder event) {
        AvtaleHendelseUtførtAvRolle rolle = AvtaleHendelseUtførtAvRolle.VEILEDER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale annullert av veileder, avtaleId={}, avtalepart={}, tiltakstype={}, opphav={}", event.getAvtale().getId(), rolle, tiltakstype, event.getAvtale().getOpphav());
        counter("avtale.annullert.annullertAvVeileder", rolle, tiltakstype).increment();
    }

    @EventListener
    public void avtaleAnnullertAvSystem(AnnullertAvSystem event) {
        AvtaleHendelseUtførtAvRolle rolle = AvtaleHendelseUtførtAvRolle.SYSTEM;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale annullert av system, avtaleId={}, avtalepart={}, tiltakstype={}, opphav={}", event.getAvtale().getId(), rolle, tiltakstype, event.getAvtale().getOpphav());
        counter("avtale.annullert.annullertAvSystem", rolle, tiltakstype).increment();
    }

    @EventListener
    public void avtaleForlenget(AvtaleForlengetAvVeileder event) {
        AvtaleHendelseUtførtAvRolle rolle = AvtaleHendelseUtførtAvRolle.VEILEDER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale forlenget, avtaleId={}, avtalepart={}, tiltakstype={}, opphav={}", event.getAvtale().getId(), rolle, tiltakstype, event.getAvtale().getOpphav());
        counter("avtale.forlenget", rolle, tiltakstype).increment();
    }

    @EventListener
    public void avtaleForkortet(AvtaleForkortetAvVeileder event) {
        AvtaleHendelseUtførtAvRolle rolle = AvtaleHendelseUtførtAvRolle.VEILEDER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale forkortet, avtaleId={}, avtalepart={}, tiltakstype={}, opphav={}", event.getAvtale().getId(), rolle, tiltakstype, event.getAvtale().getOpphav());
        counter("avtale.forkortet", rolle, tiltakstype).increment();
    }


    @EventListener
    public void avtaleNyVeileder(AvtaleNyVeileder event) {
        AvtaleHendelseUtførtAvRolle rolle = AvtaleHendelseUtførtAvRolle.VEILEDER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        if (event.getTidligereVeileder() == null) {
            log.info("Avtale tildelt veileder: avtaleId={}, veileder={}, opphav={}", event.getAvtale().getId(), event.getAvtale().getVeilederNavIdent().asString(), event.getAvtale().getOpphav());
        } else {
            log.info("Avtale byttet veileder: avtaleId={}, tidligere veileder={}, ny veileder={}, opphav={}", event.getAvtale().getId(), event.getTidligereVeileder().asString(), event.getAvtale().getVeilederNavIdent().asString(), event.getAvtale().getOpphav());
        }
        counter("avtale.endretVEileder", rolle, tiltakstype).increment();
    }
    @EventListener
    public void avtaleSlettemerket(AvtaleSlettemerket event) {
        log.info("Avtale slettemerket, utfortAv={}, avtaleId={}", event.getUtfortAv().asString(), event.getAvtale().getId());
    }

    private Counter counter(String navn, AvtaleHendelseUtførtAvRolle utfortAvRolle, Tiltakstype tiltakstype) {
        var builder = Counter.builder("tiltaksgjennomforing." + navn)
                .tag("tiltak", tiltakstype.name())
                .tag("avtalepart", utfortAvRolle.name());
        return builder.register(meterRegistry);
    }

}
