package no.nav.tag.tiltaksgjennomforing.datadeling;

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
public class AvtaleMeldingKafkaProdusent {

    private final KafkaTemplate<String, String> aivenKafkaTemplate;
    private final AvtaleMeldingEntitetRepository repository;

    public AvtaleMeldingKafkaProdusent(@Autowired @Qualifier("aivenKafkaTemplate") KafkaTemplate<String, String> aivenKafkaTemplate, AvtaleMeldingEntitetRepository repository) {
        this.aivenKafkaTemplate = aivenKafkaTemplate;
        this.repository = repository;
    }

    @TransactionalEventListener
    public void avtaleMeldingOpprettet(AvtaleMeldingOpprettet event) {
        String meldingId = event.getEntitet().getAvtaleId().toString();

        aivenKafkaTemplate.send(Topics.AVTALE_HENDELSE, meldingId, event.getEntitet().getJson()).whenComplete(
                (result, ex) -> {
                    if (ex != null) {
                        log.error("AvtaleHendelse med avtaleId {} kunne ikke sendes til Kafka topic {}", meldingId, Topics.AVTALE_HENDELSE);
                    } else {
                        log.info("AvtaleHendelse melding med avtaleId {} sendt til Kafka topic {}", meldingId, Topics.AVTALE_HENDELSE);
                        AvtaleMeldingEntitet entitet = event.getEntitet();
                        entitet.setSendt(true);
                        repository.save(entitet);
                    }
                });

        aivenKafkaTemplate.send(Topics.AVTALE_HENDELSE_COMPACT, meldingId, event.getEntitet().getJson()).whenComplete(
                (result, ex) -> {
                    if (ex != null) {
                        log.error("AvtaleHendelse med avtaleId {} kunne ikke sendes til Kafka topic {}", meldingId, Topics.AVTALE_HENDELSE_COMPACT);
                    } else {
                        log.info("AvtaleHendelse melding med avtaleId {} sendt til Kafka topic {}", meldingId, Topics.AVTALE_HENDELSE_COMPACT);
                        AvtaleMeldingEntitet entitet = event.getEntitet();
                        entitet.setSendtCompacted(true);
                        repository.save(entitet);
                    }
                });
    }
}
