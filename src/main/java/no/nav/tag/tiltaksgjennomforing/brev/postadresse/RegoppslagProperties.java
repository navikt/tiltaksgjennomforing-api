package no.nav.tag.tiltaksgjennomforing.brev.postadresse;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.regoppslag")
public class RegoppslagProperties {
	@NotNull(message = "tiltaksgjennomforing.regoppslag.uri må være satt")
	private URI uri;
}
