package no.nav.tag.tiltaksgjennomforing.arena.logging;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementAggregate;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ArenaAgreementLoggingAspect {
    private static final String ARENA_TILTAKGJENNOMFORING_ID = "arena-tiltakgjennomforing-id";
    private static final String ARENA_TILTAKDELTAKER_ID = "arena-tiltakdeltaker-id";
    private static final String ARENA_SAK_ID = "arena-sak-id";

    @Around("@annotation(arenaAgreementLogging)")
    public Object logArenaEvent(ProceedingJoinPoint joinPoint, ArenaAgreementLogging arenaAgreementLogging) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Object arg0 = args[0];

        if (!(arg0 instanceof ArenaAgreementAggregate agreementAggregate)) {
            return joinPoint.proceed();
        }

        beforeProcess(agreementAggregate);
        Object proceed = joinPoint.proceed();
        afterProcess();
        return proceed;
    }

    private void beforeProcess(ArenaAgreementAggregate agreement) {
        MDC.put(ARENA_TILTAKGJENNOMFORING_ID, agreement.getTiltakgjennomforingId().toString());
        MDC.put(ARENA_TILTAKDELTAKER_ID, agreement.getTiltakdeltakerId().toString());
        MDC.put(ARENA_SAK_ID, agreement.getSakId().toString());
    }

    private void afterProcess() {
        MDC.remove(ARENA_TILTAKGJENNOMFORING_ID);
        MDC.remove(ARENA_TILTAKDELTAKER_ID);
        MDC.remove(ARENA_SAK_ID);
    }

}