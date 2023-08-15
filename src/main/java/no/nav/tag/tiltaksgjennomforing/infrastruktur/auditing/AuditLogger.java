package no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing;

public interface AuditLogger {
    public void logg(AuditEntry event);
}
