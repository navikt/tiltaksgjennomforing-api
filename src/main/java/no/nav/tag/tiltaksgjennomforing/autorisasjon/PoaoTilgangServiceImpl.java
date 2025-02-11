package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.rest.client.RestClient;
import no.nav.poao_tilgang.client.Decision;
import no.nav.poao_tilgang.client.NavAnsattTilgangTilEksternBrukerPolicyInput;
import no.nav.poao_tilgang.client.PoaoTilgangCachedClient;
import no.nav.poao_tilgang.client.PoaoTilgangClient;
import no.nav.poao_tilgang.client.PoaoTilgangHttpClient;
import no.nav.poao_tilgang.client.TilgangType;
import no.nav.security.token.support.client.core.ClientProperties;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@Profile(value = { Miljø.DEV_FSS, Miljø.PROD_FSS })
@Slf4j
public class PoaoTilgangServiceImpl implements PoaoTilgangService {

    private final PoaoTilgangClient klient;

    public PoaoTilgangServiceImpl(
            @Value("${tiltaksgjennomforing.poao-tilgang.url}") String poaoTilgangUrl,
            ClientConfigurationProperties clientConfigurationProperties, OAuth2AccessTokenService oAuth2AccessTokenService
    ) {
        ClientProperties clientProperties = clientConfigurationProperties.getRegistration().get("poao-tilgang");
        klient = new PoaoTilgangCachedClient(
                new PoaoTilgangHttpClient(poaoTilgangUrl,
                        () -> oAuth2AccessTokenService.getAccessToken(clientProperties).getAccessToken(),
                        RestClient.baseClient()),
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(30))
                        .build(),
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(30))
                        .build(),
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(30))
                        .build());
    }

    public boolean harSkrivetilgang(UUID beslutterAzureUUID, String deltakerFnr) {
        return Optional.ofNullable(hentSkrivetilgang(beslutterAzureUUID, deltakerFnr))
            .map(Decision::isPermit)
            .orElse(false);
    }

    public Optional<String> hentGrunn(UUID beslutterAzureUUID, String deltakerFnr) {
        return Optional.ofNullable(hentSkrivetilgang(beslutterAzureUUID, deltakerFnr))
            .map(decision -> {
                if (decision.isDeny() && decision instanceof Decision.Deny deny) {
                    return deny.getReason();
                } else {
                    return null;
                }
            });
    }

    private Decision hentSkrivetilgang(UUID beslutterAzureUUID, String deltakerFnr) {
        return klient.evaluatePolicy(new NavAnsattTilgangTilEksternBrukerPolicyInput(
            beslutterAzureUUID,
            TilgangType.SKRIVE,
            deltakerFnr)
        ).get();
    }
}
