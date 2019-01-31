package no.nav.tag.tiltaksgjennomforing;

import lombok.experimental.UtilityClass;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@UtilityClass
public class Utils {
    public static <T> T sjekkAtIkkeNull(T in, String feilmelding) {
        if (in == null) {
            throw new TiltaksgjennomforingException(feilmelding);
        }
        return in;
    }

    public static boolean erIkkeNull(Object... objekter) {
        for (Object objekt : objekter) {
            if (objekt == null) {
                return false;
            }
        }
        return true;
    }

    public static URI lagUri(String path) {
        return UriComponentsBuilder
                .fromPath(path)
                .build()
                .toUri();
    }
}
