package no.nav.tag.tiltaksgjennomforing.domene;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.domene.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.domene.Tiltaktype;
import no.nav.tag.tiltaksgjennomforing.domene.events.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MetrikkRegistrering {
    private final MeterRegistry meterRegistry;

    public MetrikkRegistrering(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

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
    public void godkjenningerOpphevet(GodkjenningerOpphevet event) {
        log.info("Avtalens godkjenninger opphevet, avtaleId={} avtalepart={}", event.getAvtale().getId(), event.getUtfortAv());
        counter("avtale.godkjenning.opphevet", event.getUtfortAv()).increment();
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

    private Counter counter(String navn, Avtalerolle avtalerolle) {
        return Counter.builder("tiltaksgjennomforing." + navn)
                .tag("tiltak", Tiltaktype.ARBEIDSTRENING.name())
                .tag("avtalepart", avtalerolle.name())
                .register(meterRegistry);
    }
}
