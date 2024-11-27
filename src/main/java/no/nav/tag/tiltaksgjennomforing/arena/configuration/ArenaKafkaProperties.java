package no.nav.tag.tiltaksgjennomforing.arena.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tiltaksgjennomforing.arena.kafka")
public class ArenaKafkaProperties {
    private String tiltakdeltakerEndretTopic;
    private String tiltakgjennomforingEndretTopic;
}
