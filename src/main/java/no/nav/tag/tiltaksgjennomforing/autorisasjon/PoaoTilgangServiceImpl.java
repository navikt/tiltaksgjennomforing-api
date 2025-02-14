package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.rest.client.RestClient;
import no.nav.poao_tilgang.client.Decision;
import no.nav.poao_tilgang.client.NavAnsattTilgangTilEksternBrukerPolicyInput;
import no.nav.poao_tilgang.client.PoaoTilgangCachedClient;
import no.nav.poao_tilgang.client.PoaoTilgangClient;
import no.nav.poao_tilgang.client.PoaoTilgangHttpClient;
import no.nav.poao_tilgang.client.PolicyRequest;
import no.nav.poao_tilgang.client.PolicyResult;
import no.nav.poao_tilgang.client.TilgangType;
import no.nav.security.token.support.client.core.ClientProperties;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public boolean harSkrivetilgang(UUID beslutterAzureUUID, Fnr fnr) {
        return Optional.ofNullable(hentSkrivetilgang(beslutterAzureUUID, fnr.asString()))
            .map(Decision::isPermit)
            .orElse(false);
    }

    public Map<Fnr, Boolean> harSkrivetilgang(UUID beslutterAzureUUID, Set<Fnr> fnrListe) {
        return hentSkrivetilganger(beslutterAzureUUID, fnrListe)
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().isPermit()));
    }

    public Optional<String> hentGrunn(UUID beslutterAzureUUID, Fnr fnr) {
        return Optional.ofNullable(hentSkrivetilgang(beslutterAzureUUID, fnr.asString()))
            .map(decision -> {
                if (decision.isDeny() && decision instanceof Decision.Deny deny) {
                    return deny.getReason();
                } else {
                    return null;
                }
            });
    }

    private Map<Fnr, Decision> hentSkrivetilganger(UUID beslutterAzureUUID, Set<Fnr> fnrListe) {
       Map<UUID, Fnr> requestIdOgFnr = new HashMap<>();

        List<PolicyRequest> policyRequestList = fnrListe.stream()
            .map(fnr -> {
                UUID requestId = UUID.randomUUID();
                requestIdOgFnr.put(requestId, fnr);

                return new PolicyRequest(
                    requestId,
                    new NavAnsattTilgangTilEksternBrukerPolicyInput(
                        beslutterAzureUUID,
                        TilgangType.SKRIVE,
                        fnr.asString()
                    )
                );
            })
            .toList();

        return Optional.ofNullable(klient.evaluatePolicies(policyRequestList).get())
            .map(policyResults -> policyResults.stream()
                .collect(Collectors.toMap(
                    policyResult -> requestIdOgFnr.get(policyResult.getRequestId()),
                    PolicyResult::getDecision
                ))
            )
            .orElse(Collections.emptyMap());
    }

    private Decision hentSkrivetilgang(UUID beslutterAzureUUID, String fnr) {
        return klient.evaluatePolicy(new NavAnsattTilgangTilEksternBrukerPolicyInput(
            beslutterAzureUUID,
            TilgangType.SKRIVE,
            fnr)
        ).get();
    }
}
