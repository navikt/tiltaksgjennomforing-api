package no.nav.security.oidc.test.support.spring;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Denne interceptor gjør at direkte kall til backend automatisk blir autentisert. Skal kun brukes i dev.
 */
@Component
public class DevTokenRedirectInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (response.getStatus() == HttpServletResponse.SC_UNAUTHORIZED
                && requestErIkkeGjennomProxy(request)) {
            response.sendRedirect(request.getServletContext().getContextPath() + "/local/isso-cookie?redirect="
                    + originalRequestUri(request));
            return false;
        }
        return true;
    }

    private static String originalRequestUri(HttpServletRequest request) {
        return request.getAttribute("javax.servlet.forward.request_uri").toString();
    }

    private static boolean requestErIkkeGjennomProxy(HttpServletRequest request) {
        return request.getHeader("x-forwarded-host") == null;
    }
}
