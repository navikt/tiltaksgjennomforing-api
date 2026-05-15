package no.nav.tag.tiltaksgjennomforing.postadresse;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.regoppslag")
public class RegoppslagProperties {
	private URI uri;
}
