package no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.kafka.Topics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
public class AuditKafkaLogger implements AuditLogger {
    private final KafkaTemplate<String, String> auditKafkaTemplate;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public AuditKafkaLogger(KafkaTemplate<String, String> kafkaTemplate) {
        this.auditKafkaTemplate = kafkaTemplate;
    }

    @Override
    public void logg(AuditEntry event) {
        try {
            auditKafkaTemplate.send(Topics.AUDIT_HENDELSE, mapper.writeValueAsString(event)).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Audit-hendelse kunne ikke sendes til Kafka topic {}", Topics.AUDIT_HENDELSE, ex);
                }
            });
        } catch (JsonProcessingException ex) {
            log.error("Audit-hendelse kunne ikke serialiseres til Kafkamelding", ex);
        }
    }
}
