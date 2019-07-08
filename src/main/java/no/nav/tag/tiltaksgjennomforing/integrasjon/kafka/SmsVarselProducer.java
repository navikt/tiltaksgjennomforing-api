package no.nav.tag.tiltaksgjennomforing.integrasjon.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Profile("kafka")
@Component
@Slf4j
@RequiredArgsConstructor
public class SmsVarselProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendSmsVarselMeldingTilKafka(SmsVarselMelding smsVarsel) {
        kafkaTemplate.executeInTransaction(operations -> operations.send(MessageBuilder.withPayload(smsVarsel).setHeader(KafkaHeaders.TOPIC, Topics.SMS_VARSEL).build()));
        log.info("SmsVarsel med smsVarselId={} sendt på Kafka topic", smsVarsel.getSmsVarselId());
    }
}
