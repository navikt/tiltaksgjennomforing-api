package no.nav.tag.tiltaksgjennomforing.domene;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.domene.events.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MetrikkRegistrering {
    private final MeterRegistry meterRegistry;

    @EventListener
    public void avtaleOpprettet(AvtaleOpprettet event) {
        log.info("Avtale opprettet, avtaleId={} ident={}", event.getAvtale().getId(), event.getUtfortAv());
        counter("avtale.opprettet", Avtalerolle.VEILEDER).increment();
    }

    @EventListener
    public void avtaleEndret(AvtaleEndret event) {
        log.info("Avtale endret, avtaleId={} avtalepart={}", event.getAvtale().getId(), event.getUtfortAv());
        counter("avtale.endret", event.getUtfortAv()).increment();
    }

    @EventListener
    public void godkjenningerOpphevetAvArbeidsgiver(GodkjenningerOpphevetAvArbeidsgiver event) {
        Avtalerolle rolle = Avtalerolle.ARBEIDSGIVER;
        log.info("Avtalens godkjenninger opphevet, avtaleId={} avtalepart={}", event.getAvtale().getId(), rolle);
        counter("avtale.godkjenning.opphevet", rolle).increment();
    }

    @EventListener
    public void godkjenningerOpphevetAvVeileder(GodkjenningerOpphevetAvVeileder event) {
        Avtalerolle rolle = Avtalerolle.VEILEDER;
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
        log.info("Avtale godkjent, avtaleId={} avtalepart=VEILEDER", event.getAvtale().getId());
        counter("avtale.godkjenning.godkjent", Avtalerolle.VEILEDER).increment();
    }

    @EventListener
    public void godkjentPaVegneAv(GodkjentPaVegneAv event) {
        log.info("Avtale godkjent på vegne av deltaker, avtaleId={} avtalepart=VEILEDER", event.getAvtale().getId());
        counter("avtale.godkjenning.godkjentPaVegneAv", Avtalerolle.VEILEDER).increment();
    }

    private Counter counter(String navn, Avtalerolle avtalerolle) {
        return Counter.builder("tiltaksgjennomforing." + navn)
                .tag("tiltak", Tiltaktype.ARBEIDSTRENING.name())
                .tag("avtalepart", avtalerolle.name())
                .register(meterRegistry);
    }
}
