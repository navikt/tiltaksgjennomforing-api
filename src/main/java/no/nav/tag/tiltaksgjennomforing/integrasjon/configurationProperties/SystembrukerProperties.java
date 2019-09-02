package no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.consumer.system")
public class SystembrukerProperties {

    private String id;
}
