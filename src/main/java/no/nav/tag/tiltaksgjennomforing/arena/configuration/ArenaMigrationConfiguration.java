package no.nav.tag.tiltaksgjennomforing.arena.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ArenaMigrationConfiguration {

    @Bean(name = "arenaThreadPoolExecutor")
    public Executor arenaThreadPoolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("ArenaMigration-");
        executor.initialize();
        return executor;
    }
}
