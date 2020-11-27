package no.nav.tag.tiltaksgjennomforing.refusjon;


import java.util.UUID;
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
public class RefusjonProducer {

  private final KafkaTemplate<String, Refusjonsmelding> aivenKafkaTemplate;
  private final FeatureToggleService featureToggleService;

  public void publiserRefusjonsmelding(Refusjonsmelding refusjonsmelding) {
    boolean brukSendingAvRefusjonsmeldinger = featureToggleService.isEnabled("arbeidsgiver.tiltaksgjennomforing-api.refusjon");
    if (!brukSendingAvRefusjonsmeldinger) {
      log.warn(
          "Feature arbeidsgiver.tiltaksgjennomforing-api.refusjon er ikke aktivert. Sender derfor ikke en refusjonsmelding til Kafka topic.");
      return;
    }
    aivenKafkaTemplate.send(Topics.REFUSJON, refusjonsmelding.getTilskuddPeriodeId().toString(), refusjonsmelding)
        .addCallback(new ListenableFutureCallback<>() {
          @Override
          public void onFailure(Throwable ex) {
            log.warn("Refusjonsmelding med Tilskudd Periode Id={} kunne ikke sendes til Kafka topic", refusjonsmelding.getTilskuddPeriodeId());
          }

          @Override
          public void onSuccess(SendResult<String, Refusjonsmelding> result) {
            log.info("Refusjonsmelding med Tilskudd Periode Id={} sendt til Kafka topic", refusjonsmelding.getTilskuddPeriodeId());
          }
        });
  }

}
