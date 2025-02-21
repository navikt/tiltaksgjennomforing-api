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
}
