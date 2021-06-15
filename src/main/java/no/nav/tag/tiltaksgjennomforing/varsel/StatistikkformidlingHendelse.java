package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentPaVegneAv;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentPaVegneAvArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentPaVegneAvDeltakerOgArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.varsel.kafka.StatistikkformidlingProducer;
import no.nav.tag.tiltaksgjennomforing.varsel.kafka.Statistikkformidlingsmelding;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
@RequiredArgsConstructor
public class StatistikkformidlingHendelse {

    private final StatistikkformidlingProducer statistikkformidlingProducer;

    @TransactionalEventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) {
        statistikkformidlingProducer.publiserStatistikkformidlingMelding(Statistikkformidlingsmelding.fraAvtale(event.getAvtale()));
    }

    @TransactionalEventListener
    public void godkjentPaVegneAv(GodkjentPaVegneAv event) {
        statistikkformidlingProducer.publiserStatistikkformidlingMelding(Statistikkformidlingsmelding.fraAvtale(event.getAvtale()));
    }

    @TransactionalEventListener
    public void godkjentPaVegneAvArbeidsgiver(GodkjentPaVegneAvArbeidsgiver event) {
        statistikkformidlingProducer.publiserStatistikkformidlingMelding(Statistikkformidlingsmelding.fraAvtale(event.getAvtale()));
    }

    @TransactionalEventListener
    public void godkjentPaVegneAvDeltakerOgArbeidsgiver(GodkjentPaVegneAvDeltakerOgArbeidsgiver event) {
        statistikkformidlingProducer.publiserStatistikkformidlingMelding(Statistikkformidlingsmelding.fraAvtale(event.getAvtale()));
    }
}
