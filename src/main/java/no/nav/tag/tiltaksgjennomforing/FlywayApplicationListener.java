package no.nav.tag.tiltaksgjennomforing;


import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class FlywayApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private DataSource dataSource;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        Flyway
                .configure()
                .dataSource(dataSource)
                .load()
                .migrate();
    }
}
