package no.nav.tag.tiltaksgjennomforing.domene;

import no.nav.tag.tiltaksgjennomforing.domene.varsel.VarslbarHendelse;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.event.AfterSaveEvent;
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent;

@Configuration
public class AvtaleSaveListener {


    @Bean
    public ApplicationListener<BeforeSaveEvent> saveAvtaleListener() {
        return event -> {
            Object entity = event.getEntity();
            if (entity instanceof Avtale) {
                Avtale avtale = (Avtale) entity;
                avtale.settIdOgOpprettetTidspunkt();
            } else if (entity instanceof VarslbarHendelse) {
                VarslbarHendelse varslbarHendelse = (VarslbarHendelse) entity;
                varslbarHendelse.settIdOgOpprettetTidspunkt();
            }
        };
    }
}
