package no.nav.tag.tiltaksgjennomforing.metrikker;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.avtale.events.*;
import no.nav.tag.tiltaksgjennomforing.varsel.SmsVarselRepository;
import no.nav.tag.tiltaksgjennomforing.varsel.events.SmsVarselResultatMottatt;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
@RequiredArgsConstructor
public class MetrikkRegistrering {
    private final MeterRegistry meterRegistry;
    private final SmsVarselRepository smsVarselRepository;

    @PostConstruct
    public void init() {
        Gauge.builder("tiltaksgjennomforing.smsvarsel.usendt", smsVarselRepository::antallUsendte)
                .register(meterRegistry);
    }

    @EventListener
    public void smsVarselResultatMottatt(SmsVarselResultatMottatt event) {
        switch (event.getSmsVarsel().getStatus()) {
            case SENDT:
                smsVarselSendt();
                break;
            case FEIL:
                smsVarselFeil();
                break;
        }
    }

    public void smsVarselSendt() {
        Counter.builder("tiltaksgjennomforing.smsvarsel.sendt").register(meterRegistry).increment();
    }

    public void smsVarselFeil() {
        Counter.builder("tiltaksgjennomforing.smsvarsel.feil").register(meterRegistry).increment();
    }

    @EventListener
    public void avtaleOpprettet(AvtaleOpprettetAvVeileder event) {
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale opprettet, avtaleId={} ident={}, tiltakstype={}", event.getAvtale().getId(), event.getUtfortAv(), tiltakstype);
        counter("avtale.opprettet", Avtalerolle.VEILEDER, tiltakstype).increment();
    }

    @EventListener
    public void avtaleEndret(AvtaleEndret event) {
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale endret, avtaleId={}, avtalepart={}, tiltakstype={}", event.getAvtale().getId(), event.getUtfortAv(), tiltakstype);
        counter("avtale.endret", event.getUtfortAv(), tiltakstype).increment();
    }

    @EventListener
    public void godkjenningerOpphevet(GodkjenningerOpphevetAvVeileder event) {
        Avtalerolle rolle = Avtalerolle.VEILEDER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtalens godkjenninger opphevet, avtaleId={}, avtalepart={}, tiltakstype={}", event.getAvtale().getId(), rolle, tiltakstype);
        counter("avtale.godkjenning.opphevet", rolle, tiltakstype).increment();
    }

    @EventListener
    public void godkjenningerOpphevet(GodkjenningerOpphevetAvArbeidsgiver event) {
        Avtalerolle rolle = Avtalerolle.ARBEIDSGIVER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtalens godkjenninger opphevet, avtaleId={}, avtalepart={}, tiltakstype={}", event.getAvtale().getId(), rolle, tiltakstype);
        counter("avtale.godkjenning.opphevet", rolle, tiltakstype).increment();
    }

    @EventListener
    public void godkjentAvDeltaker(GodkjentAvDeltaker event) {
        Avtalerolle rolle = Avtalerolle.DELTAKER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale godkjent, avtaleId={}, avtalepart={}, tiltakstype={}", event.getAvtale().getId(), rolle, tiltakstype);
        counter("avtale.godkjenning.godkjent", rolle, tiltakstype).increment();
    }

    @EventListener
    public void godkjentAvArbeidsgiver(GodkjentAvArbeidsgiver event) {
        Avtalerolle rolle = Avtalerolle.ARBEIDSGIVER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale godkjent, avtaleId={}, avtalepart={}, tiltakstype={}", event.getAvtale().getId(), rolle, tiltakstype);
        counter("avtale.godkjenning.godkjent", rolle, tiltakstype).increment();
    }

    @EventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) {
        Avtalerolle rolle = Avtalerolle.VEILEDER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale godkjent, avtaleId={}, avtalepart={}, tiltakstype={}", event.getAvtale().getId(), rolle, tiltakstype);
        counter("avtale.godkjenning.godkjent", rolle, tiltakstype).increment();
    }

    @EventListener
    public void godkjentPaVegneAv(GodkjentPaVegneAv event) {
        Avtalerolle rolle = Avtalerolle.VEILEDER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale godkjent på vegne av deltaker, avtaleId={}, avtalepart={}, tiltakstype={}", event.getAvtale().getId(), rolle, tiltakstype);
        counter("avtale.godkjenning.godkjentPaVegneAv", rolle, tiltakstype).increment();
    }

    @EventListener
    public void avtaleLåstOpp(AvtaleLåstOpp event) {
        Avtalerolle rolle = Avtalerolle.VEILEDER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale låst opp, avtaleId={}, avtalepart={}, tiltakstype={}", event.getAvtale().getId(), rolle, tiltakstype);
        counter("avtale.laastOpp", rolle, tiltakstype).increment();
    }

    @EventListener
    public void avtaleDeltMedAvtalepart(AvtaleDeltMedAvtalepart event) {
        Avtalerolle rolle = Avtalerolle.VEILEDER;
        Tiltakstype tiltakstype = event.getAvtale().getTiltakstype();
        log.info("Avtale delt med {}, avtaleId={}, avtalepart={}, tiltakstype={}", event.getAvtalepart(), event.getAvtale().getId(), rolle, tiltakstype);
        counter("avtale.deltMedAvtalepart", rolle, tiltakstype).increment();
    }

    private Counter counter(String navn, Avtalerolle avtalerolle, Tiltakstype tiltakstype) {
        var builder = Counter.builder("tiltaksgjennomforing." + navn)
                .tag("tiltak", tiltakstype.name())
                .tag("avtalepart", avtalerolle.name());
        return builder.register(meterRegistry);
    }

}
