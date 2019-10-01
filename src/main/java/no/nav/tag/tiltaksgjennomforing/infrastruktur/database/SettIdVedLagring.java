package no.nav.tag.tiltaksgjennomforing.infrastruktur.database;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.varsel.BjelleVarsel;
import no.nav.tag.tiltaksgjennomforing.varsel.SmsVarsel;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelse;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent;

@Configuration
public class SettIdVedLagring {

    @Bean
    public ApplicationListener<BeforeSaveEvent> saveAvtaleListener() {
        return event -> {
            Object entity = event.getEntity();
            if (entity instanceof Avtale) {
                ((Avtale) entity).settIdOgOpprettetTidspunkt();
            } else if (entity instanceof VarslbarHendelse) {
                ((VarslbarHendelse) entity).settIdOgOpprettetTidspunkt();
            } else if (entity instanceof SmsVarsel) {
                ((SmsVarsel) entity).settId();
            } else if (entity instanceof BjelleVarsel) {
                ((BjelleVarsel) entity).settId();
            }
        };
    }
}
