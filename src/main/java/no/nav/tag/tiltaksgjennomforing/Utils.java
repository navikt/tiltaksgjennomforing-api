package no.nav.tag.tiltaksgjennomforing;

import lombok.experimental.UtilityClass;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@UtilityClass
public class Utils {
    public static <T> T ikkeNull(T in, String feilmelding) {
        if (in == null) {
            throw new TiltaksgjennomforingException(feilmelding);
        }
        return in;
    }

    public static URI lagUri(String path) {
        return UriComponentsBuilder
                .fromPath(path)
                .build()
                .toUri();
    }
}
