package no.nav.tag.tiltaksgjennomforing.varsel.kafka;


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
public class StatistikkformidlingProducer {

  private final KafkaTemplate<String, Statistikkformidlingsmelding> kafkaTemplate;

  public void publiserStatistikkformidlingMelding(Statistikkformidlingsmelding melding) {
    kafkaTemplate.send(Topics.STATISTIKKFORMIDLING, melding.getAvtaleId(), melding)
        .addCallback(new ListenableFutureCallback<>() {
          @Override
          public void onFailure(Throwable ex) {
            log.warn("Statistikkformidlingsmelding med avtaleID={} kunne ikke sendes til Kafka topic", melding.getAvtaleId());
          }

          @Override
          public void onSuccess(SendResult<String, Statistikkformidlingsmelding> result) {
            log.info("Statistikkformidlingsmelding med avtaleID={} sendt på Kafka topic", melding.getAvtaleId());
          }
            });
    }
}
