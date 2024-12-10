package no.nav.tag.tiltaksgjennomforing.varsel.oppgave;

import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GosysVarselConfig {
    private static final String GOSYS_OPPGAVE = "GOSYS_OPPGAVE";

    @Bean
    OneTimeTask<OppgaveRequest> opprettOppgave(OppgaveVarselService oppgaveVarselService) {
        return Tasks.oneTime(GOSYS_OPPGAVE, OppgaveRequest.class)
                .execute((inst, ctx) -> {
                    oppgaveVarselService.opprettOppgave(inst.getData());
                    log.info("Opprettet en Gosys-oppgave: {}", inst.getId());
                });
    }
}
