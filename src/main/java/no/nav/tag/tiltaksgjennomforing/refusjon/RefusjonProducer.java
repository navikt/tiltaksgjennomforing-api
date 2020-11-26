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

  private final KafkaTemplate<String, Refusjon> aivenKafkaTemplate;

  public void publiserRefusjonsmelding(Refusjon refusjon) {
    aivenKafkaTemplate.send(Topics.REFUSJON, UUID.randomUUID().toString(), refusjon)
        .addCallback(new ListenableFutureCallback<>() {
          @Override
          public void onFailure(Throwable ex) {
            log.warn("Refusjonsmelding med ID={} kunne ikke sendes til Kafka topic", refusjon);
          }

          @Override
          public void onSuccess(SendResult<String, Refusjon> result) {
            log.info("Refusjonsmelding med ID={} sendt til Kafka topic", refusjon);
          }
        });
  }

}
