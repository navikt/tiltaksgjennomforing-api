package no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing;

public record AuditEntry(
        String brukerId,
        String it, java.time.Instant utførtTid, String s, java.net.URI uri,
        org.springframework.http.HttpMethod httpMethod, String traceId) {}
