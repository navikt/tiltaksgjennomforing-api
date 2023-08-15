package no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "tiltaksgjennomforing.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class AuditVoidLogger implements AuditLogger{
    @Override
    public void logg(AuditEntry event) {

    }
}
