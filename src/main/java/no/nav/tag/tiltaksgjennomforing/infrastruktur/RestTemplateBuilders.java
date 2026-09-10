package no.nav.tag.tiltaksgjennomforing.infrastruktur;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.restclient.RestTemplateBuilder;

public final class RestTemplateBuilders {

    private RestTemplateBuilders() {
    }

    /**
     * Spring Framework 7's {@code JdkClientHttpRequestFactory} (default siden verken
     * Apache HttpClient5 eller Jetty er på classpath) slår på automatisk
     * gzip/deflate-dekomprimering. Enkelte interne Nav-tjenester (bl.a.
     * arbeidsgiver-altinn-tilganger) svarer med {@code Content-Encoding: gzip} uten at
     * body faktisk er gzippet på veien (proxy/sidecar), noe som gir
     * {@code java.util.zip.ZipException: incorrect header check}. Skrur derfor av
     * klient-side dekomprimering for å gjenopprette oppførselen fra Spring Boot 3.
     */
    public static RestTemplateBuilder utenKomprimering(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.requestFactoryBuilder(ClientHttpRequestFactoryBuilder.jdk()
            .withCustomizer(factory -> factory.enableCompression(false)));
    }
}
