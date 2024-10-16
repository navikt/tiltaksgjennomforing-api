package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.kafka.Topics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
public class DvhMeldingKafkaProdusent {
    private final KafkaTemplate<String, AvroTiltakHendelse> dvhMeldingKafkaTemplate;
    private final DvhMeldingEntitetRepository repository;

    public DvhMeldingKafkaProdusent(@Autowired @Qualifier("dvhMeldingKafkaTemplate") KafkaTemplate<String, AvroTiltakHendelse> dvhMeldingKafkaTemplate, DvhMeldingEntitetRepository repository) {
        this.dvhMeldingKafkaTemplate = dvhMeldingKafkaTemplate;
        this.repository = repository;
    }

    @TransactionalEventListener
    public void dvhMeldingOpprettet(DvhMeldingOpprettet event) {
        String meldingId = event.getAvroTiltakHendelse().getMeldingId();
        String topic = Topics.DVH_MELDING;
        dvhMeldingKafkaTemplate.send(topic, meldingId, event.getAvroTiltakHendelse()).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("DvhMelding med id {} kunne ikke sendes til Kafka topic {}", meldingId, topic);
            } else {
                log.info("DvhMelding med id {} sendt til Kafka topic {}", meldingId, topic);
                DvhMeldingEntitet entitet = event.getEntitet();
                entitet.setSendt(true);
                repository.save(entitet);
            }
        });
    }
}
