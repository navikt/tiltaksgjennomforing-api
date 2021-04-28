package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
public class DvhMeldingKafkaProdusent {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @TransactionalEventListener
    public void dvhMeldingOpprettet(DvhMeldingOpprettet event) {
        String meldingId = event.getAvroTiltakHendelse().getMeldingId();
        String topic = Topics.DVH_MELDING;
        kafkaTemplate.send(topic, meldingId, event.getAvroTiltakHendelse().toString()).addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                log.info("DvhMelding med id {} sendt til Kafka topic {}", meldingId, topic);
            }

            @Override
            public void onFailure(Throwable ex) {
                log.warn("DvhMelding med id {} kunne ikke sendes til Kafka topic {}", meldingId, topic);
            }
        });;
    }
}
