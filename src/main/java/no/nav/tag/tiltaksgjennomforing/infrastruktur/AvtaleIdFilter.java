package no.nav.tag.tiltaksgjennomforing.infrastruktur;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class AvtaleIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain chain
    ) throws ServletException, IOException {
        Optional.ofNullable(request.getParameter("avtaleId"))
            .filter(this::isUuid)
            .ifPresent(uuid -> MDC.put("avtale-id", uuid));

        chain.doFilter(request, response);
    }

    private boolean isUuid(String avtaleId) {
        try {
            UUID.fromString(avtaleId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
