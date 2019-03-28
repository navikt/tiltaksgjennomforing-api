package no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.database")
public class DatabaseProperties {
    private String databaseNavn;
    private String databaseUrl;
    private String vaultSti;
    private Integer maximumPoolSize;
    private Integer minimumIdle;
    private Integer maxLifetime;
}
