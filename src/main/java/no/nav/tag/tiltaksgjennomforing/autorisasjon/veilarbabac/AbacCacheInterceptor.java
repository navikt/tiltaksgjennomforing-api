package no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac;

import static java.util.Optional.ofNullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile(value= {"dev", "preprod"})
public class AbacCacheInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    private static final String CLEAR_CACHE_HEADER = "x-abac-clear-cache";
    private final VeilarbabacClient veilarbabacClient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(ofNullable(request.getHeader(CLEAR_CACHE_HEADER)).map(Boolean::valueOf).orElse(false)) {
            veilarbabacClient.cacheEvict();
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }
    
}
