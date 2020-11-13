package no.nav.tag.tiltaksgjennomforing.varsel;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.events.GodkjentAvVeileder;
import no.nav.tag.tiltaksgjennomforing.varsel.kafka.StatistikkFormidlingProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
@RequiredArgsConstructor
public class StatistikkHendelseFormidling {

    private final StatistikkFormidlingProducer statistikkFormidlingProducer;

    @TransactionalEventListener
    public void godkjentAvVeileder(GodkjentAvVeileder event) {
        statistikkFormidlingProducer.sendStatistikkFormidlingMeldingTilKafka(event.getAvtale());
    }

}
