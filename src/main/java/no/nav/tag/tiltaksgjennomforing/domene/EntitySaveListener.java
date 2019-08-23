package no.nav.tag.tiltaksgjennomforing.domene;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent;

@Configuration
public class EntitySaveListener {

    @Bean
    public ApplicationListener<BeforeSaveEvent> saveAvtaleListener() {
        return event -> {
            Object entity = event.getEntity();
            if (entity instanceof IdOgTidspunktGenerator) {
                ((IdOgTidspunktGenerator)entity).settIdOgOpprettetTidspunkt();
            }
        };
    }
}
