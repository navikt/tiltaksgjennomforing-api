package no.nav.tag.tiltaksgjennomforing.tilskudd;


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
public class TilskuddProducer {

  private final KafkaTemplate<String, TilskuddMelding> aivenKafkaTemplate;
  private final FeatureToggleService featureToggleService;

  public void publiserTilskuddMelding(TilskuddMelding tilskuddMelding) {
    boolean brukSendingAvTilskuddMelding = featureToggleService.isEnabled("arbeidsgiver.tiltaksgjennomforing-api.refusjon");
    if (!brukSendingAvTilskuddMelding) {
      log.warn(
          "Feature arbeidsgiver.tiltaksgjennomforing-api.refusjon er ikke aktivert. Sender derfor ikke en refusjonsmelding til Kafka topic.");
      return;
    }
    aivenKafkaTemplate.send(Topics.REFUSJON, tilskuddMelding.getTilskuddPeriodeId().toString(), tilskuddMelding)
        .addCallback(new ListenableFutureCallback<>() {
          @Override
          public void onFailure(Throwable ex) {
            log.warn("Refusjonsmelding med Tilskudd Periode Id={} kunne ikke sendes til Kafka topic", tilskuddMelding.getTilskuddPeriodeId());
          }

          @Override
          public void onSuccess(SendResult<String, TilskuddMelding> result) {
            log.info("Refusjonsmelding med Tilskudd Periode Id={} sendt til Kafka topic", tilskuddMelding.getTilskuddPeriodeId());
          }
        });
  }

}
