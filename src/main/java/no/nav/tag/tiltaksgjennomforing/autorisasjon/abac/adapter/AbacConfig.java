package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.adapter;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.abac")
public class AbacConfig {
  private String uri;
}
