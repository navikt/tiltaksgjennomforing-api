package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.varsel.events.SmsVarselOpprettet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
@RequiredArgsConstructor
public class OpprettSmsVarselKafkaMelding {
    private final SmsVarselProducer producer;
    private final SmsVarselMeldingMapper mapper;

    @TransactionalEventListener
    public void opprettMelding(SmsVarselOpprettet event) {
        producer.sendSmsVarselMeldingTilKafka(mapper.tilMelding(event.getSmsVarsel()));
    }
}
