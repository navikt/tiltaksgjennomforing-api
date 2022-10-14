package no.nav.tag.tiltaksgjennomforing.datadeling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
public class AvtaleMeldingKafkaProdusent {

    private final KafkaTemplate<String, String> aivenKafkaTemplate;
    private final AvtaleMeldingEntitetRepository repository;

    public AvtaleMeldingKafkaProdusent(@Autowired @Qualifier("aivenKafkaTemplate") KafkaTemplate<String, String> aivenKafkaTemplate, AvtaleMeldingEntitetRepository repository) {
        this.aivenKafkaTemplate = aivenKafkaTemplate;
        this.repository = repository;
    }

    @TransactionalEventListener
    public void avtaleMeldingOpprettet(AvtaleMeldingOpprettet event) {
        //String topic =;
        //aivenKafkaTemplate.send();
    }
}
