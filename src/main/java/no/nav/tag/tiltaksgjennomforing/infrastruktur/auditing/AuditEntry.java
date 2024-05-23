package no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing;

import java.net.URI;
import java.time.Instant;

public record AuditEntry(
        String appNavn,
        String utførtAv,
        String oppslagPå,
        EventType eventType,
        boolean forespørselTillatt,
        Instant oppslagUtførtTid,
        String beskrivelse,
        URI requestUrl,
        String requestMethod,
        String correlationId
) {
}
