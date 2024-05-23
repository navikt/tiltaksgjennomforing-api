package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.NotifikasjonService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LokalConfiguration {

  @Bean
  NotifikasjonService notifikasjon() { return Mockito.mock(NotifikasjonService.class);}

}
