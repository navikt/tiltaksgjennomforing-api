package no.nav.tag.tiltaksgjennomforing.domene;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent;

import java.util.UUID;

@Configuration
public class AvtaleSaveListener {

    @Bean
    public ApplicationListener<BeforeSaveEvent> saveAvtaleListener() {
        return event -> {
            Object entity = event.getEntity();
            if (entity instanceof Avtale) {
                Avtale avtale = (Avtale) entity;
                if (avtale.getId() == null) {
                    avtale.setId(UUID.randomUUID());
                }

                avtale.getMaal().forEach(Maal::settIdOgOpprettetTidspunkt);
                avtale.getOppgaver().forEach(Oppgave::settIdOgOpprettetTidspunkt);
            }
        };
    }
}
