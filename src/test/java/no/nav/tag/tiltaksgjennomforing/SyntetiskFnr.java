package no.nav.tag.tiltaksgjennomforing;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tillater syntetiske fødselsnumre i hver test i klassen.
 * Setter FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS til true før hver test og false etter.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(SyntetiskFnrExtension.class)
public @interface SyntetiskFnr {
}
