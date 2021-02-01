package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsperiodeGodkjent;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.concurrent.ListenableFutureCallback;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
@Slf4j
@RequiredArgsConstructor
public class TilskuddsperiodeKafkaProducer {

    private final KafkaTemplate<String, TilskuddsperiodeGodkjentMelding> aivenKafkaTemplate;
    private final FeatureToggleService featureToggleService;

    @TransactionalEventListener
    public void publiserTilskuddsperiodeGodkjentMelding(TilskuddsperiodeGodkjent event) {
        TilskuddsperiodeGodkjentMelding tilskuddMelding = TilskuddsperiodeGodkjentMelding.fraAvtale(event.getAvtale());
        publiserTilskuddsperiodeGodkjentMelding(tilskuddMelding);
    }

    public void publiserTilskuddsperiodeGodkjentMelding(TilskuddsperiodeGodkjentMelding tilskuddMelding) {
        boolean brukSendingAvTilskuddMelding = featureToggleService.isEnabled("arbeidsgiver.tiltaksgjennomforing-api.refusjon");
        if (!brukSendingAvTilskuddMelding) {
            log.warn(
                    "Feature arbeidsgiver.tiltaksgjennomforing-api.refusjon er ikke aktivert. Sender derfor ikke en refusjonsmelding til Kafka topic.");
            return;
        }
        aivenKafkaTemplate.send(Topics.REFUSJON, tilskuddMelding.getTilskuddsperiodeId().toString(), tilskuddMelding)
                .addCallback(new ListenableFutureCallback<>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        log.warn("Refusjonsmelding med Tilskudd Periode Id={} kunne ikke sendes til Kafka topic", tilskuddMelding.getTilskuddsperiodeId());
                    }

                    @Override
                    public void onSuccess(SendResult<String, TilskuddsperiodeGodkjentMelding> result) {
                        log.info("Refusjonsmelding med Tilskudd Periode Id={} sendt til Kafka topic", tilskuddMelding.getTilskuddsperiodeId());
                    }
                });
    }

}
