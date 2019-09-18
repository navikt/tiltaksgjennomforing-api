package no.nav.tag.tiltaksgjennomforing.metrikker;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltaktype;
import no.nav.tag.tiltaksgjennomforing.avtale.events.*;
import no.nav.tag.tiltaksgjennomforing.varsel.SmsVarselRepository;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.pilottilgang.PilotProperties;
import no.nav.tag.tiltaksgjennomforing.varsel.events.SmsVarselResultatMottatt;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
@RequiredArgsConstructor
public class MetrikkRegistrering {
    private final MeterRegistry meterRegistry;
    private final PilotProperties pilotProperties;
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

    private boolean pilotFylke(Identifikator utfortAv) {
        return !pilotProperties.getIdenter().contains(utfortAv);
    }

    @EventListener
    public void avtaleOpprettet(AvtaleOpprettet event) {
        log.info("Avtale opprettet, avtaleId={} ident={}, PilotFylke={}", event.getAvtale().getId(), event.getUtfortAv(), pilotFylke(event.getUtfortAv()));
        counter("avtale.opprettet", Avtalerolle.VEILEDER).increment();
    }

    @EventListener
    public void avtaleEndret(AvtaleEndret event) {
        log.info("Avtale endret, avtaleId={} avtalepart={}", event.getAvtale().getId(), event.getUtfortAv());
        counter("avtale.endret", event.getUtfortAv()).increment();
    }

    @EventListener
    public void godkjenningerOpphevet(GodkjenningerOpphevetAvVeileder event) {
        Avtalerolle rolle = Avtalerolle.VEILEDER;
        log.info("Avtalens godkjenninger opphevet, avtaleId={} avtalepart={}", event.getAvtale().getId(), rolle);
        counter("avtale.godkjenning.opphevet", rolle).increment();
    }

    @EventListener
    public void godkjenningerOpphevet(GodkjenningerOpphevetAvArbeidsgiver event) {
        Avtalerolle rolle = Avtalerolle.ARBEIDSGIVER;
        log.info("Avtalens godkjenninger opphevet, avtaleId={} avtalepart={}", event.getAvtale().getId(), rolle);
        counter("avtale.godkjenning.opphevet", rolle).increment();
    }

    @EventListener
    public void godkjentAvDeltaker(GodkjentAvDeltaker event) {
        log.info("Avtale godkjent, avtaleId={} avtalepart=DELTAKER", event.getAvtale().getId());
        counter("avtale.godkjenning.godkjent", Avtalerolle.DELTAKER).increment();
    }

    @EventListener
    public void godkjentAvArbeidsgiver(GodkjentAvArbeidsgiver event) {
        log.info("Avtale godkjent, avtaleId={} avtalepart=ARBEIDSGIVER", event.getAvtale().getId());
        counter("avtale.godkjenning.godkjent", Avtalerolle.ARBEIDSGIVER).increment();
    }

    @EventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) {
        log.info("Avtale godkjent, avtaleId={} avtalepart=VEILEDER, PilotFylke={}", event.getAvtale().getId(), pilotFylke(event.getUtfortAv()));
        counter("avtale.godkjenning.godkjent", Avtalerolle.VEILEDER).increment();
    }

    @EventListener
    public void godkjentPaVegneAv(GodkjentPaVegneAv event) {
        log.info("Avtale godkjent på vegne av deltaker, avtaleId={} avtalepart=VEILEDER, PilotFylke={}", event.getAvtale().getId(), pilotFylke(event.getUtfortAv()));
        counter("avtale.godkjenning.godkjentPaVegneAv", Avtalerolle.VEILEDER).increment();
    }

    private Counter counter(String navn, Avtalerolle avtalerolle) {
        var builder = Counter.builder("tiltaksgjennomforing." + navn)
                .tag("tiltak", Tiltaktype.ARBEIDSTRENING.name())
                .tag("avtalepart", avtalerolle.name());
        return builder.register(meterRegistry);
    }

}
