package no.nav.tag.tiltaksgjennomforing.infrastruktur;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing.AuditEntry;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing.AuditLogger;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.auditing.EventType;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static no.nav.tag.tiltaksgjennomforing.infrastruktur.CorrelationIdSupplier.MDC_CORRELATION_ID_KEY;

/**
 * Dette filteret fanger opp alle responser fra APIet.
 * Dersom en person (arbeidsgiver, saksbehandler) har gjort et oppslag
 * og får returnert en JSON som inneholder "deltakerFnr" så vil dette
 * resultere i en audit-hendelse.
 */
@Slf4j
@Component
class AuditLoggingFilter extends OncePerRequestFilter {
    private final TokenUtils tokenUtils;
    private final AuditLogger auditLogger;
    private static final String classname = AuditLoggingFilter.class.getName();

    public AuditLoggingFilter(TokenUtils tokenUtils, AuditLogger auditLogger) {
        this.tokenUtils = tokenUtils;
        this.auditLogger = auditLogger;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var wrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrapper);
        String correlationId;
        if (request.getAttribute(MDC_CORRELATION_ID_KEY) != null) correlationId = (String) request.getAttribute(MDC_CORRELATION_ID_KEY);
        else correlationId = null;

        if (correlationId == null) {
            log.error("{}: feilet pga manglende correlationId.", classname);
        }
        if (response.getContentType() != null && response.getContentType().startsWith("application/json") && correlationId != null) {
            try {
                List<String> fnr = JsonPath.read(wrapper.getContentInputStream(), "$..deltakerFnr");
                var utførtTid = Now.instant();
                String brukerId = tokenUtils.hentBrukerOgIssuer().map(TokenUtils.BrukerOgIssuer::getBrukerIdent).orElse(null);
                var uri = URI.create(request.getRequestURI());
                // Logger kun oppslag dersom en innlogget bruker utførte oppslaget
                if (brukerId != null) {
                    fnr.forEach(it -> {
                        // Ikke logg at en bruker slår opp sin egen informasjon
                        if (!brukerId.equals(it)) {
                            var entry = new AuditEntry(
                                    "tiltaksgjennomforing-api",
                                    brukerId,
                                    it,
                                    EventType.READ,
                                    true,
                                    utførtTid,
                                    msgForUri(uri),
                                    uri,
                                    HttpMethod.valueOf(request.getMethod()),
                                    correlationId
                            );
                            auditLogger.logg(entry);
                        }
                    });
                }
            } catch (IOException ex) {
                log.warn("{}: Klarte ikke dekode responsen. Var det ikke gyldig JSON?", classname);
            } catch (Exception ex) {
                log.error("{}: Logging feilet", classname, ex);
            }
        }
        wrapper.copyBodyToResponse();
    }

    private static String msgForUri(URI uri) {
        var uriString = uri.toString();
        if (uriString.contains("/avtaler/deltaker-allerede-paa-tiltak")) {
            return "Undersøk om deltaker allerede er på arbeidsmarkedstiltak. I forbindelse med opprettelse av avtale";
        } else if(uriString.matches("/avtaler/\\w+")){
            return "Hent detaljer for avtale for arbeidsmarkedstiltak";
        } else if (uriString.contains("/avtaler") || uriString.contains("avtaler/beslutter-liste")) {
            return "Hent liste over avtaler for arbeidsmarkedstiltak";
        } else {
            log.warn("{}: Fant ikke en lesbar melding for uri: {}", classname, uri);
            return "Oppslag i løsning for tiltaksgjennomføring";
        }
    }
}