package no.nav.tag.tiltaksgjennomforing.refusjon;


import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
@Slf4j
@RequiredArgsConstructor
public class RefusjonProducer {

  private final KafkaTemplate<String, String> aivenKafkaTemplate;

  public void publiserRefusjonsmelding(String melding) {
    aivenKafkaTemplate.send(Topics.REFUSJON, UUID.randomUUID().toString(), melding)
        .addCallback(new ListenableFutureCallback<>() {
          @Override
          public void onFailure(Throwable ex) {
            log.warn("Refusjonsmelding med ID={} kunne ikke sendes til Kafka topic", melding);
          }

          @Override
          public void onSuccess(SendResult<String, String> result) {
            log.info("Refusjonsmelding med ID={} sendt til Kafka topic", melding);
          }
        });
  }

}
