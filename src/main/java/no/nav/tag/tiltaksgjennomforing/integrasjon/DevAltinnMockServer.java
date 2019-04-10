package no.nav.tag.tiltaksgjennomforing.integrasjon;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile({"mock"})
@Slf4j
@Component
public class DevAltinnMockServer {
    private final WireMockServer server;

    public DevAltinnMockServer() {
        log.info("Starter Altinn mockserver.");
        server = new WireMockServer(WireMockConfiguration.options().usingFilesUnderClasspath(".").port(8090));
        server.start();
    }
}
