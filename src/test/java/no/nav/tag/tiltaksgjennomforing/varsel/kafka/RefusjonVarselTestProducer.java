package no.nav.tag.tiltaksgjennomforing.varsel.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.kafka.Topics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
@Component
@Slf4j
public class RefusjonVarselTestProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public RefusjonVarselTestProducer(
        KafkaTemplate<String, String> kafkaTemplate,
        ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publiserMelding(String meldingId, RefusjonVarselMelding refusjonVarselMelding) throws JsonProcessingException {
        String melding = objectMapper.writeValueAsString(refusjonVarselMelding);

        kafkaTemplate.send(Topics.TILTAK_VARSEL, meldingId, melding).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Melding med id {} kunne ikke sendes til Kafka topic {}", meldingId, Topics.TILTAK_VARSEL);
                log.error("Feilmelding: ", ex);
            } else {
                log.info("Melding med id {} sendt til Kafka topic {}", meldingId, Topics.TILTAK_VARSEL);
            }
        });
    }
}
