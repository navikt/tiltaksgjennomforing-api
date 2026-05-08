package no.nav.tag.tiltaksgjennomforing.infrastruktur;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LegacyContextPathRedirectFilterTest {

    private LegacyContextPathRedirectFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new LegacyContextPathRedirectFilter(new SimpleMeterRegistry());
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
    }

    @Test
    void shouldRewriteLegacyContextPath() throws ServletException, IOException {
        // Given
        request.setRequestURI("/tiltaksgjennomforing-api/avtaler");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(argThat(req -> {
            HttpServletRequest httpReq = (HttpServletRequest) req;
            return "/avtaler".equals(httpReq.getRequestURI()) &&
                   "/avtaler".equals(httpReq.getServletPath());
        }), eq(response));
    }

    @Test
    void shouldRewriteLegacyContextPathWithQueryString() throws ServletException, IOException {
        // Given
        request.setRequestURI("/tiltaksgjennomforing-api/avtaler/123");
        request.setQueryString("page=1&size=10");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(argThat(req -> {
            HttpServletRequest httpReq = (HttpServletRequest) req;
            return "/avtaler/123".equals(httpReq.getRequestURI()) &&
                   "page=1&size=10".equals(httpReq.getQueryString());
        }), eq(response));
    }

    @Test
    void shouldRewriteLegacyContextPathRootToRoot() throws ServletException, IOException {
        // Given
        request.setRequestURI("/tiltaksgjennomforing-api");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(argThat(req -> {
            HttpServletRequest httpReq = (HttpServletRequest) req;
            return "/".equals(httpReq.getRequestURI());
        }), eq(response));
    }

    @Test
    void shouldNotRewriteNonLegacyPaths() throws ServletException, IOException {
        // Given
        request.setRequestURI("/avtaler");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(eq(request), eq(response));
    }

    @Test
    void shouldRewriteInternalHealthcheckPath() throws ServletException, IOException {
        // Given
        request.setRequestURI("/tiltaksgjennomforing-api/internal/healthcheck");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(argThat(req -> {
            HttpServletRequest httpReq = (HttpServletRequest) req;
            return "/internal/healthcheck".equals(httpReq.getRequestURI());
        }), eq(response));
    }
}


