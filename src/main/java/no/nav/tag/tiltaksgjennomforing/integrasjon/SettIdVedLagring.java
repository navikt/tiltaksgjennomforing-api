package no.nav.tag.tiltaksgjennomforing.integrasjon;

import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.BjelleVarsel;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.SmsVarsel;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.VarslbarHendelse;
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
