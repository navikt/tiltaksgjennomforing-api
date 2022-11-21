package no.nav.tag.tiltaksgjennomforing.datadeling;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.kafka.Topics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
@Slf4j
@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Profile({Miljø.DEV_FSS, Miljø.LOCAL, "dockercompose"})
public class AvtaleMeldingKafkaProdusent {

    private final KafkaTemplate<String, String> aivenKafkaTemplate;
    private final AvtaleMeldingEntitetRepository repository;

    public AvtaleMeldingKafkaProdusent(@Autowired @Qualifier("aivenKafkaTemplate") KafkaTemplate<String, String> aivenKafkaTemplate, AvtaleMeldingEntitetRepository repository) {
        this.aivenKafkaTemplate = aivenKafkaTemplate;
        this.repository = repository;
    }

    @TransactionalEventListener
    public void avtaleMeldingOpprettet(AvtaleMeldingOpprettet event) {
        String meldingId = event.getEntitet().getMeldingId().toString();

        aivenKafkaTemplate.send(Topics.AVTALE_HENDELSE, meldingId, event.getEntitet().getJson()).addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                log.info("AvtaleHendelse melding med id {} sendt til Kafka topic {}", meldingId, Topics.AVTALE_HENDELSE);
                AvtaleMeldingEntitet entitet = event.getEntitet();
                entitet.setSendt(true);
                repository.save(entitet);
            }
            @Override
            public void onFailure(Throwable ex) {
                log.error("AvtaleHendelse med id {} kunne ikke sendes til Kafka topic {}", meldingId, Topics.AVTALE_HENDELSE);
            }
        });

    }
}
