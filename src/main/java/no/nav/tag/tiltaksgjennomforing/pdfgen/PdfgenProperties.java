package no.nav.tag.tiltaksgjennomforing.pdfgen;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.pdfgen")
public class PdfgenProperties {
    private URI uri;
}
