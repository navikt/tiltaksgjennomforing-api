package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.kafka.Topics;
import no.nav.tag.tiltaksgjennomforing.varsel.Sms;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
@Slf4j
public class SmsProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public SmsProducer(
        KafkaTemplate<String, String> kafkaTemplate,
        ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendSmsVarselMeldingTilKafka(Sms sms) throws JsonProcessingException {
        String melding = objectMapper.writeValueAsString(sms);
        kafkaTemplate.send(Topics.TILTAK_SMS, sms.getSmsVarselId().toString(), melding).whenComplete((result, ex) -> {
            if (ex != null) {
                log.warn("Sms med id={} kunne ikke sendes til Kafka topic", sms.getSmsVarselId());
            } else {
                log.info(
                    "Sms med id={} sendt på Kafka topic {}",
                    sms.getSmsVarselId(),
                    result.getProducerRecord().topic()
                );
            }
        });
    }
}
