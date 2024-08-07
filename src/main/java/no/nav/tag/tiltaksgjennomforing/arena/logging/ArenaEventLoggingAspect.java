package no.nav.tag.tiltaksgjennomforing.arena.logging;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ArenaEventLoggingAspect {
    private static final String ARENA_EVENT_KEY = "arena-event";
    private static final String ARENA_ID_KEY = "arena-id";
    private static final String ARENA_OPERATION_KEY = "arena-operation";

    @Around("@annotation(arenaEventLogging)")
    public Object logArenaEvent(ProceedingJoinPoint joinPoint, ArenaEventLogging arenaEventLogging) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Object arg0 = args[0];

        if (!(arg0 instanceof ArenaEvent arenaEvent)) {
            return joinPoint.proceed();
        }

        beforeProcess(arenaEvent);
        Object proceed = joinPoint.proceed();
        afterProcess();
        return proceed;
    }

    private void beforeProcess(ArenaEvent arenaEvent) {
        MDC.put(ARENA_EVENT_KEY, arenaEvent.getId().toString());
        MDC.put(ARENA_ID_KEY, arenaEvent.getLogId());
        MDC.put(ARENA_OPERATION_KEY, arenaEvent.getOperation().name());
    }

    private void afterProcess() {
        MDC.remove(ARENA_EVENT_KEY);
        MDC.remove(ARENA_ID_KEY);
        MDC.remove(ARENA_OPERATION_KEY);
    }

}
