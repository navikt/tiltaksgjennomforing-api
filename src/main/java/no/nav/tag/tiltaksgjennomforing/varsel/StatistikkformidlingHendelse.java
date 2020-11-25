package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentAvVeileder;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentPaVegneAv;
import no.nav.tag.tiltaksgjennomforing.refusjon.RefusjonProducer;
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
    private final RefusjonProducer refusjonProducer;

    @TransactionalEventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) {
        refusjonProducer.publiserRefusjonsmelding("TEST1");
        statistikkformidlingProducer.publiserStatistikkformidlingMelding(Statistikkformidlingsmelding.fraAvtale(event.getAvtale()));
    }

    @TransactionalEventListener
    public void godkjentPaVegneAv(GodkjentPaVegneAv event) {
        refusjonProducer.publiserRefusjonsmelding("TEST2");
        statistikkformidlingProducer.publiserStatistikkformidlingMelding(Statistikkformidlingsmelding.fraAvtale(event.getAvtale()));
    }
}
