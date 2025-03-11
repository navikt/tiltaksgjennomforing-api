package no.nav.tag.tiltaksgjennomforing.persondata.domene;

import lombok.SneakyThrows;
import lombok.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Value
public class PdlRequest<V> {
    public record Varaibles(String ident) {}
    public record BolkVariables(List<String> identer) {}

    private final String query;
    private final V variables;

    public PdlRequest(String query, V variables) {
        this.query = query;
        this.variables = variables;
    }

    public PdlRequest(Resource resource, V variables) {
        this(resourceAsString(resource), variables);
    }

    @SneakyThrows
    private static String resourceAsString(Resource resource) {
        String filinnhold = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        return filinnhold.replaceAll("\\s+", " ");
    }

}
