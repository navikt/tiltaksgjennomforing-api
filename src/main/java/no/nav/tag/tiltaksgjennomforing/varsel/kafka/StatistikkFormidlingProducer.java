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
public class StatistikkFormidlingProducer {

    private final KafkaTemplate<String, StatistikkFormidlingsmelding> kafkaTemplate;

    public void sendStatistikkFormidlingMeldingTilKafka(Avtale avtale) {
        StatistikkFormidlingsmelding statistikkFormidlingsmelding = getFormidlingsmeldingFraAvtale(avtale);
        kafkaTemplate.send(Topics.STATISTIKK_FORMIDLING, avtale.getId().toString(), statistikkFormidlingsmelding)
            .addCallback(new ListenableFutureCallback<>() {
                @Override
                public void onFailure(Throwable ex) {
                    log.warn("Statistikk formidlingsmelding med avtaleID={} kunne ikke sendes til Kafka topic", avtale.getId().toString());
                }

                @Override
                public void onSuccess(SendResult<String, StatistikkFormidlingsmelding> result) {
                    log.info("Statistikk formidlingsmelding med avtaleID={} sendt på Kafka topic", avtale.getId().toString());
                }
            });
    }

    @NotNull
    private StatistikkFormidlingsmelding getFormidlingsmeldingFraAvtale(Avtale avtale) {
        return new StatistikkFormidlingsmelding(
            avtale.getBedriftNr().toString(), avtale.getStillingstype(),
            avtale.getStillingstittel(), avtale.getLonnstilskuddProsent(),
            avtale.getTiltakstype(), avtale.getId().toString(),
            String.format("%s %s", avtale.getDeltakerFornavn(), avtale.getDeltakerEtternavn()));
    }
}
