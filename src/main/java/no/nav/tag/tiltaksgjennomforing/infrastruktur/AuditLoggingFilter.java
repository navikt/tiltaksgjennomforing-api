package no.nav.tag.tiltaksgjennomforing.infrastruktur;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;

import java.util.List;

import no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing.AuditEntry;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing.AuditLogger;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

/**
 * Dette filteret fanger opp alle responser fra APIet.
 * Dersom en person (arbeidsgiver, saksbehandler) har gjort et oppslag
 * og får returnert en JSON som inneholder "deltakerFnr" så vil dette
 * resultere i en audit-hendelse.
 */
@Slf4j
@Component
class AuditLoggingFilter extends OncePerRequestFilter {
    private final TokenValidationContextHolder context;
    private final AuditLogger auditLogger;
    private static final String classname = AuditLoggingFilter.class.getName();

    public AuditLoggingFilter(TokenValidationContextHolder context, AuditLogger auditLogger) {
        this.context = context;
        this.auditLogger = auditLogger;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var wrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrapper);
        String traceId;
        if (MDC.get("traceId") != null) traceId = MDC.get("traceId");
        else if (request.getAttribute("traceId") != null) traceId = (String) request.getAttribute("traceId");
        else traceId = null;

        if (traceId == null) {
            log.error("$className: feilet pga manglende traceId. Sjekk om OpenTelemetry er riktig konfigurert eller om rekkefølgen på spring-filtre er riktig");
        }
        if (Objects.equals(response.getContentType(), "application/json") && traceId != null) {
            try {
                List<String> fnr = JsonPath.read(wrapper.getContentInputStream(), "$..deltakerFnr");
                var utførtTid = Now.instant();
                String brukerId;
                if (context.getTokenValidationContext().hasTokenFor("tokenx")) {
                    brukerId = context.getTokenValidationContext().getClaims("tokenx").getStringClaim("pid");
                } else if (context.getTokenValidationContext().hasTokenFor("aad")) {
                    brukerId = context.getTokenValidationContext().getClaims("aad").getStringClaim("NAVident");
                } else {
                    brukerId = null;
                }
                var uri = URI.create(request.getRequestURI());
                // Logger kun oppslag dersom en innlogget bruker utførte oppslaget
                if (brukerId != null) {
                    fnr.forEach(it -> {
                        var entry = new AuditEntry(
                                brukerId,
                                it,
                                utførtTid,
                                msgForUri(uri),
                                uri,
                                HttpMethod.valueOf(request.getMethod()),
                                traceId
                        );
                        auditLogger.logg(entry);
                    });
                }
            } catch (IOException ex) {
                log.warn("$className: Klarte ikke dekode responsen. Var det ikke gyldig JSON?");
            } catch (Exception ex) {
                log.error("$className: Logging feilet", ex);
            }
        }
        wrapper.copyBodyToResponse();
    }

    private static String msgForUri(URI uri) {
        if (uri.toString().contains("/refusjon/hentliste")) {
            return "Oppslag på refusjoner";
        } else if (uri.toString().contains("/refusjon/\\w+")) {
            return "Hent detaljer om refusjon";
        } else if (uri.toString().contains("/refusjon/")) {
            return "Oppslag på refusjoner";
        } else if (uri.toString().contains("/korreksjon/")) {
            return "Oppslag på korreksjoner";
        } else if (uri.toString().contains("/korreksjon/\\w+")) {
            return "Hent detaljer om korreksjon";
        } else {
            log.warn("{}: Fant ikke en lesbar melding for uri: {}", classname, uri);
            return "Oppslag i refusjonsløsning";
        }
    }
}