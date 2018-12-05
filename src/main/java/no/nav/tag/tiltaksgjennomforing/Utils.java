package no.nav.tag.tiltaksgjennomforing;

import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

public class Utils {

    public static URI lagUri(String path) {
        return UriComponentsBuilder
                .fromPath(path)
                .build()
                .toUri();
    }
}
