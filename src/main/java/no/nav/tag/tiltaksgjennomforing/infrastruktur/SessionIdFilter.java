package no.nav.tag.tiltaksgjennomforing.infrastruktur;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;
import org.slf4j.MDC;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class SessionIdFilter extends OncePerRequestFilter {
    private static final String HEADER_NAME = "X-Session-Id";

    private final Environment env;
    private final TokenUtils tokenUtils;

    public SessionIdFilter(Environment env, TokenUtils tokenUtils) {
        this.env = env;
        this.tokenUtils = tokenUtils;
    }

    @Override
    protected void doFilterInternal(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain chain
    ) throws ServletException, IOException {
        tokenUtils.hentJwtToken().map(token -> {
            HashCode hashCode = Hashing.sha256().hashString(token.getEncodedToken(), StandardCharsets.UTF_8);
            return UUID.nameUUIDFromBytes(hashCode.asBytes());
        }).ifPresent(uuid -> {
            MDC.put("session-id", uuid.toString());
            if (env.matchesProfiles(Miljø.LOCAL, Miljø.DEV_FSS, Miljø.DEV_GCP_LABS, Miljø.DOCKER_COMPOSE)) {
                response.setHeader(HEADER_NAME, uuid.toString());
            }
        });
        chain.doFilter(request, response);
    }
}
