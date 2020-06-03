package no.nav.tag.tiltaksgjennomforing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnOrganisasjon;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringProperties;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Profile("wiremock")
@Slf4j
@Component
public class AltinnProxyOgAltinnMockServer implements DisposableBean {
    private static final boolean VERBOSE_WIREMOCK = false;
    private static final String ORGANISASJONER_PATH = "/ekstern/altinn/api/serviceowner/reportees";

    private final WireMockServer server;
    private final AltinnTilgangsstyringProperties altinnTilgangsstyringProperties;

    public AltinnProxyOgAltinnMockServer(AltinnTilgangsstyringProperties altinnTilgangsstyringProperties) {
        this.altinnTilgangsstyringProperties = altinnTilgangsstyringProperties;
        log.info("Starter mockserver for Altinn proxy integrasjon.");
        server = new WireMockServer(
                WireMockConfiguration.options()
                        .port(8091)
                        .notifier(new ConsoleNotifier(VERBOSE_WIREMOCK))
        );
        server.start();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void destroy() {
        log.info("Stopper mockserver.");
        server.stop();
    }

    /* Altinn-proxy Stubs */
    @SneakyThrows
    public void stubForAltinnProxy(
            String usersToken,
            String serviceCode,
            String serviceEdition,
            AltinnOrganisasjon... response
    ) {
        server.stubFor(
                get(
                        getReporteesUrlForAltinnProxy(
                                altinnTilgangsstyringProperties.getProxyUrl().getPath(),
                                serviceCode,
                                serviceEdition
                        )
                )
                        .withHeader("Authorization", equalTo("Bearer " + usersToken))
                        .willReturn(
                                okJson(
                                        objectMapper.writeValueAsString(List.of(response))
                                )
                        )
        );
    }

    @SneakyThrows
    public void stubForAltinnProxyUnavailable(String usersToken, String serviceCode, String serviceEdition) {
        server.stubFor(
                get(
                        getReporteesUrlForAltinnProxy(
                                altinnTilgangsstyringProperties.getProxyUrl().getPath(),
                                serviceCode,
                                serviceEdition
                        )
                )
                        .withHeader("Authorization", equalTo("Bearer " + usersToken))
                        .willReturn(serviceUnavailable())
        );
    }

    @SneakyThrows
    public void stubForAltinnProxyBadRequest(String usersToken, String serviceCode, String serviceEdition) {
        server.stubFor(
                get(
                        getReporteesUrlForAltinnProxy(
                                altinnTilgangsstyringProperties.getProxyUrl().getPath(),
                                serviceCode,
                                serviceEdition
                        )
                )
                        .withHeader("Authorization", equalTo("Bearer " + usersToken))
                        .willReturn(badRequest())
        );
    }

    /* Altinn Stubs (fallback) */
    @SneakyThrows
    public void stubForAltinn(
            Fnr fnr,
            String serviceCode,
            String serviceEdition,
            AltinnOrganisasjon... response
    ) {
        server.stubFor(
                get(
                        getReporteesUrlForAltinn(
                                altinnTilgangsstyringProperties.getProxyFallbackUrl().getPath(),
                                fnr.asString(),
                                serviceCode,
                                serviceEdition
                        )
                )
                        .willReturn(
                                okJson(
                                        objectMapper.writeValueAsString(List.of(response))
                                )
                        )
        );
    }

    @SneakyThrows
    public void stubForAltinnBadRequest(Fnr fnr, String serviceCode, String serviceEdition) {
        server.stubFor(
                get(
                        getReporteesUrlForAltinn(
                                altinnTilgangsstyringProperties.getProxyFallbackUrl().getPath(),
                                fnr.asString(),
                                serviceCode,
                                serviceEdition
                        )
                )
                        .willReturn(badRequest())
        );
    }



    private String getReporteesUrlForAltinnProxy(String baseUrl, String serviceCode, String serviceEdition)
            throws UnsupportedEncodingException {
        return baseUrl
                + ORGANISASJONER_PATH
                + "?"
                + encode("$skip") + "=0"
                + "&" + encode("$top") + "=500"
                + "&serviceCode=" + serviceCode
                + "&serviceEdition=" + serviceEdition
                + "&ForceEIAuthentication";
    }

    private String getReporteesUrlForAltinn(String baseUrl, String subject, String serviceCode, String serviceEdition)
            throws UnsupportedEncodingException {
        return baseUrl
                + ORGANISASJONER_PATH
                + "?"
                + encode("$skip") + "=0"
                + "&" + encode("$top") + "=500"
                + "&serviceCode=" + serviceCode
                + "&serviceEdition=" + serviceEdition
                + "&ForceEIAuthentication"
                + "&subject=" + subject;
    }

    private static String encode(String $filter) throws UnsupportedEncodingException {
        return URLEncoder.encode($filter, StandardCharsets.UTF_8.toString());
    }
}
