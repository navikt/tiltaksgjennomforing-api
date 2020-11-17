package no.nav.tag.tiltaksgjennomforing.varsel.kafka;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
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
  private final FeatureToggleService featureToggleService;

  public void publiserStatistikkformidlingMelding(Statistikkformidlingsmelding melding) {
    boolean brukStatistikkformidling = featureToggleService.isEnabled("arbeidsgiver.tiltaksgjennomforing-api.statistikkformidling");
    if (!brukStatistikkformidling) {
      log.warn("Feature arbeidsgiver.tiltaksgjennomforing-api.statistikkformidling er ikke aktivert.");
      return;
    }
    kafkaTemplate.send(Topics.STATISTIKKFORMIDLING, melding.getInnholdVersion(), melding)
        .addCallback(new ListenableFutureCallback<>() {
          @Override
          public void onFailure(Throwable ex) {
            log.warn("Statistikkformidlingsmelding med avtaleID={} kunne ikke sendes til Kafka topic", melding.getInnholdVersion());
          }

          @Override
          public void onSuccess(SendResult<String, Statistikkformidlingsmelding> result) {
            log.info("Statistikkformidlingsmelding med avtaleID={} sendt på Kafka topic", melding.getInnholdVersion());
          }
        });
  }
}
