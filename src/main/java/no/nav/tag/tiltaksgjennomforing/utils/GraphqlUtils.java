package no.nav.tag.tiltaksgjennomforing.utils;

import lombok.experimental.UtilityClass;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class GraphqlUtils {
    public static String lesQuery(String path) {
        try {
            return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Kunne ikke lese GraphQL-query " + path, e);
        }
    }
}
