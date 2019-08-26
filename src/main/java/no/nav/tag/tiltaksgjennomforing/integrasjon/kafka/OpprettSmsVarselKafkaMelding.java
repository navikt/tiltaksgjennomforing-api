package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.domene.events.SmsVarselOpprettet;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Profile("kafka")
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
