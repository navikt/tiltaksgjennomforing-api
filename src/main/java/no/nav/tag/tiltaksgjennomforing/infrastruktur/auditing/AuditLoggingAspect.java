package no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class AuditLoggingAspect {

    public AuditLoggingAspect(TokenUtils tokenUtils, AuditLogger auditLogger) {
        this.tokenUtils = tokenUtils;
        this.auditLogger = auditLogger;
    }

    private final TokenUtils tokenUtils;
    private final AuditLogger auditLogger;

    /**
     * Denne "handleren" kjøres etter at en controller-metode er ferdigkjørt, og brukes for å se om verdien som returneres
     * er en eller flere avtaler som kan logges. Hvis det er tilfellet, logges det et audit-event for hver unike kombinasjon
     * av deltaker/bedrift.
     *
     * @param joinPoint            Dette er punktet som denne handleren "henger" på. Brukes for å hente ut annotasjonsbeskrivelsen
     * @param resultatFraEndepunkt Objektet som ble returnert av controller-metoden
     */
    @AfterReturning(value = "@annotation(no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing.AuditLogging)", returning = "resultatFraEndepunkt")
    public void postProcess(JoinPoint joinPoint, Object resultatFraEndepunkt) {
        try {
            var httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

            sendAuditmeldingerTilKafka(httpServletRequest, hentAuditLoggingAnnotasjonsverdi(joinPoint), hentEntiteterSomKanAuditlogges(resultatFraEndepunkt));
        } catch (Exception ex) {
            log.error("{}: Logging feilet", this.getClass().getName(), ex);
        }
    }

    /**
     * På grunn av at Collection, HashMap og ResponseEntity er generics, er vi nødt til å kverne igjennom mange
     * instanceof-sjekker for å finne ut om responsen fra controller-metoden som wrappes av Auditlogging-annotasjonen
     * faktisk inneholder en "auditerbar" avtale.
     * <br/>
     * Hvis returverdien er en ResponseEntity eller HashMap, så "unboxer" vi disse og kaller funksjonen igjen.
     * I tilfellet hvor objektet er et HashMap prøver vi å hente ut avtaler fra "avtaler"-nøkkelen.
     */
    private Set<AuditElement> hentEntiteterSomKanAuditlogges(Object resultatobjekt) {
        if (resultatobjekt instanceof ResponseEntity<?> responseEntity) {
            // Rekursivt kall for å "unboxe" ResponseEntity
            return hentEntiteterSomKanAuditlogges(responseEntity.getBody());
        } else if (resultatobjekt instanceof Map<?, ?> hashmap) {
            // Rekursivt kall for å "unboxe" HashMap (vil sannsynligvis da treffe Collection-branchen under)
            return hentEntiteterSomKanAuditlogges(hashmap.get("avtaler"));
        }

        var entiteter = new ArrayList<AuditerbarEntitet>();
        if (resultatobjekt instanceof Collection<?> avtaler) {
            avtaler.forEach(avtale -> {
                if (avtale instanceof AuditerbarEntitet ae) {
                    entiteter.add(ae);
                }
            });
            if (avtaler.size() != entiteter.size()) {
                log.error("AuditLoggingAspect fant en respons som ikke inneholdt avtaler: {}",
                        avtaler.stream().findFirst().map(Object::getClass).map(Class::getName).orElse("null"));
            }
        } else if (resultatobjekt instanceof AuditerbarEntitet ae) {
            // Responsen var en enkelt auditentitet
            entiteter.add(ae);
        } else {
            log.error("AuditLoggingAspect støtter ikke denne typen responsobjekt: {}", resultatobjekt.getClass().getName());
        }
        return hentOppslagsdata(entiteter);
    }

    private static String hentAuditLoggingAnnotasjonsverdi(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getMethod().getAnnotation(AuditLogging.class).value();
    }

    /**
     * Konverterer auditerbare avtaler til et FnrOgBedrift-sett for å sikre at vi får ut unike
     * oppslag (hvis vi ikke gjør dette vil man feks logge oppslag mot samme deltaker i to avtaler dobbelt).
     */
    private static @NotNull Set<AuditElement> hentOppslagsdata(Collection<AuditerbarEntitet> result) {
        return result.stream().map(AuditElement::of).collect(Collectors.toSet());
    }

    private void sendAuditmeldingerTilKafka(HttpServletRequest request, String apiBeskrivelse, Set<AuditElement> auditelementer) {
        try {
            String innloggetBrukerId = tokenUtils.hentBrukerOgIssuer().map(TokenUtils.BrukerOgIssuer::getBrukerIdent).orElse(null);
            // Logger kun oppslag dersom en innlogget bruker utførte oppslaget
            if (innloggetBrukerId != null) {
                var uri = URI.create(request.getRequestURI());
                var utførtTid = Now.instant();

                var innloggetBrukerErPrivatperson = Fnr.erGyldigFnr(innloggetBrukerId);
                auditelementer.forEach(auditelement -> {
                    // Vi er ikke interessert i oppslag som bruker gjør på seg selv
                    if (auditelement.deltakerFnr().asString().equals(innloggetBrukerId)) {
                        return;
                    }
                    auditLogger.logg(
                            new AuditEntry(
                                    "tiltaksgjennomforing-api",
                                    // ArcSight vil ikke ha oppslag som er utført av en privatperson; oppslaget må derfor være "utført av" en bedrift
                                    innloggetBrukerErPrivatperson ? auditelement.bedriftNr().asString() : innloggetBrukerId,
                                    auditelement.deltakerFnr().asString(),
                                    "%s-%s".formatted(auditelement.id(), auditelement.sistEndret()),
                                    EventType.READ,
                                    true,
                                    utførtTid,
                                    apiBeskrivelse,
                                    uri,
                                    request.getMethod(),
                                    request.getAttribute("correlationId").toString()
                            )
                    );
                });
            }
        } catch (Exception ex) {
            log.error("{}: Logging feilet", this.getClass().getName(), ex);
        }
    }
}
