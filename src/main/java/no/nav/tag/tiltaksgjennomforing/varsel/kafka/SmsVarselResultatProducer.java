package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Profile("kafka")
@Component
@Slf4j
@RequiredArgsConstructor
public class SmsVarselResultatProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendSmsVarselResultatMeldingTilKafka(SmsVarselResultatMelding resultatMelding) {
        try {
            kafkaTemplate.send(
                    MessageBuilder.withPayload(resultatMelding).setHeader(KafkaHeaders.TOPIC, Topics.SMS_VARSEL_RESULTAT).build()).get();
            log.info("SmsVarselResultat med smsVarselId={} og status={} sendt på Kafka topic", resultatMelding.getSmsVarselId(), resultatMelding.getStatus());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Kunne ikke sende melding til Kafka topic", e);
            throw new TiltaksgjennomforingException("Kunne ikke sende melding til Kafka topic");
        }
    }
}
