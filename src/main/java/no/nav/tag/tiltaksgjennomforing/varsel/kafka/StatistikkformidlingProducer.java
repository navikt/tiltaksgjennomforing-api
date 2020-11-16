package no.nav.tag.tiltaksgjennomforing.varsel.kafka;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import org.jetbrains.annotations.NotNull;
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

  public void publiserStatistikkformidlingMelding(Avtale avtale) {
    Statistikkformidlingsmelding statistikkFormidlingsmelding = tilFormidlingsmelding(avtale);
    kafkaTemplate.send(Topics.STATISTIKKFORMIDLING, avtale.getId().toString(), statistikkFormidlingsmelding)
        .addCallback(new ListenableFutureCallback<>() {
          @Override
          public void onFailure(Throwable ex) {
            log.warn("Statistikkformidlingsmelding med avtaleID={} kunne ikke sendes til Kafka topic", avtale.getId().toString());
          }

          @Override
          public void onSuccess(SendResult<String, Statistikkformidlingsmelding> result) {
            log.info("Statistikkformidlingsmelding med avtaleID={} sendt på Kafka topic", avtale.getId().toString());
          }
            });
    }

  @NotNull
  private Statistikkformidlingsmelding tilFormidlingsmelding(Avtale avtale) {
    return new Statistikkformidlingsmelding(
        avtale.getBedriftNr().toString(), avtale.getStillingstype(),
        avtale.getStillingstittel(), avtale.getLonnstilskuddProsent(),
        avtale.getTiltakstype(), avtale.getId().toString(),
        String.format("%s %s", avtale.getDeltakerFornavn(), avtale.getDeltakerEtternavn()));
  }
}
