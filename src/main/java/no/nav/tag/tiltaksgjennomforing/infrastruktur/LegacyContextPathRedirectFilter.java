package no.nav.tag.tiltaksgjennomforing.infrastruktur;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Midlertidig filter for å håndtere bakoverkompatibilitet når context-path fjernes.
 * Skriver transparent over forespørsler med den gamle /tiltaksgjennomforing-api-prefiksen for å fungere med den nye rotstien.
 * Både gamle og nye stier fungerer samtidig uten omdirigering.
 * <p>
 * TODO: Fjern dette filteret etter migreringperioden (f.eks. 6 måneder etter utrulling)
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LegacyContextPathRedirectFilter extends OncePerRequestFilter {

    private final MeterRegistry meterRegistry;

    public LegacyContextPathRedirectFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    private static final String LEGACY_COUNTER = "legacy_url_path_counter";
    private static final String LEGACY_CONTEXT_PATH = "/tiltaksgjennomforing-api";

    @Value("${tiltaksgjennomforing.legacy-context-path.rewrite-enabled:true}")
    private boolean rewriteEnabled;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Vi skal skrive om url'en dersom den starter med gammel base path
        if (rewriteEnabled && requestURI.startsWith(LEGACY_CONTEXT_PATH)) {
            meterRegistry.counter(LEGACY_COUNTER).increment();
            final String newPath = requestURI.substring(LEGACY_CONTEXT_PATH.length()).isEmpty()
                    ? "/"
                    : requestURI.substring(LEGACY_CONTEXT_PATH.length());

            log.info("Gammel path brukt: {} -> omskriver til: {} (klient: {})",
                    requestURI,
                    newPath,
                    request.getRemoteAddr()
            );

            // Overstyr request med en wrapper der getterne for paths er den omskrevne versjonen.
            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
                @Override
                public String getRequestURI() {
                    return newPath;
                }

                @Override
                public String getServletPath() {
                    return newPath;
                }
            };

            filterChain.doFilter(wrappedRequest, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}



