package no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty("tiltaksgjennomforing.kafka.enabled")
public class AuditKafkaLogger implements AuditLogger {

    @Override
    public void logg(AuditEntry event) {}
}
