package no.nav.tag.tiltaksgjennomforing.infrastruktur;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class NavIdentFilter extends OncePerRequestFilter {

    private final TokenUtils tokenUtils;

    public NavIdentFilter(TokenUtils tokenUtils) {
        this.tokenUtils = tokenUtils;
    }

    @Override
    protected void doFilterInternal(
            @NotNull final HttpServletRequest request,
            @NotNull final HttpServletResponse response,
            @NotNull final FilterChain chain
    ) throws ServletException, IOException {
        try {
            try {
                tokenUtils.hentBrukerOgIssuer().ifPresent(token -> {
                    if (token.getIssuer().equals(TokenUtils.Issuer.ISSUER_AAD)) {
                        request.setAttribute("navIdent", token.getBrukerIdent());
                        MDC.put("navIdent", token.getBrukerIdent());
                    }
                });
            } catch (Exception e) {
                log.error("Kunne ikke hente ikke hente NavIdent i NavIdentFilter", e);
            }

            chain.doFilter(request, response);
        } finally {
            MDC.remove("navIdent");
        }
    }
}
