package no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLogging {
    String value();

    EventType type() default EventType.READ;

    /**
     * Hvilke utfall skal auditlogges?
     * <p>
     * Hensikten med dette feltet er å kunne utelukke auditlogging som lykkes på feks
     * opprettelse av avtale. Da vil det ikke returneres data og auditlogging vil feile
     * fordi det ikke returneres noen "auditerbare elementer".
     */
    Utfall utfall() default Utfall.ALLE;
}
