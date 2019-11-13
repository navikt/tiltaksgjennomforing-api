package no.nav.tag.tiltaksgjennomforing.persondata;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.persondata")
public class PersondataProperties {
    private String uri;
}
