package no.nav.tag.tiltaksgjennomforing.autorisasjon.poaotilgang;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.rest.client.RestClient;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PoaoTilgangHttpRetryKlient {
    private static final int HTTP_TIMEOUT_SECONDS = 15;
    private static final int MAX_RETRIES = 2;

    private PoaoTilgangHttpRetryKlient() {}

    public static OkHttpClient hentKlient() {
        return RestClient.baseClientBuilder()
                .connectTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(new RetryInterceptor())
                .build();
    }

    private static class RetryInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            IOException lastException = null;
            for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
                try {
                    return chain.proceed(chain.request());
                } catch (IOException e) {
                    lastException = e;
                    log.warn("poao-tilgang kall feilet (forsøk {}/{}): {}", attempt + 1, MAX_RETRIES + 1, e.getMessage());
                }
            }
            throw lastException;
        }
    }
}
