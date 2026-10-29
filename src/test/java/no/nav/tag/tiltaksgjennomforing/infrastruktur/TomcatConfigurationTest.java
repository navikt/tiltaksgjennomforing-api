package no.nav.tag.tiltaksgjennomforing.infrastruktur;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.web.context.request.RequestContextListener;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class TomcatConfigurationTest {

    @ParameterizedTest
    @CsvSource({"3, 200", "11, 200", "101, 200", "1000, 400"})
    void haandterer_sprakpreferanser_med_oppdatert_grense(int antallSprak, int forventetStatus) throws Exception {
        var factory = new TomcatServletWebServerFactory(0);
        new TomcatConfiguration().customize(factory);
        var server = factory.getWebServer(context -> {
            context.addListener(new RequestContextListener());
            context.addServlet("test", new HttpServlet() {
                @Override
                protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
                    response.getWriter().write("OK");
                }
            }).addMapping("/");
        });

        try {
            server.start();
            String sprak = IntStream.range(0, antallSprak)
                    .mapToObj(i -> "en-x-l" + i)
                    .collect(Collectors.joining(","));
            var request = HttpRequest.newBuilder(URI.create("http://localhost:" + server.getPort() + "/"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept-Language", sprak)
                    .GET()
                    .build();

            try (var client = HttpClient.newHttpClient()) {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                assertThat(response.statusCode()).isEqualTo(forventetStatus);
                if (forventetStatus == 200) {
                    assertThat(response.body()).isEqualTo("OK");
                }
            }
        } finally {
            server.stop();
            server.destroy();
        }
    }
}
