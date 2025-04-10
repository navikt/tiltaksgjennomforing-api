package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.rest.client.RestClient;
import no.nav.poao_tilgang.api.dto.response.TilgangsattributterResponse;
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
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Profile(value = { Miljø.DEV_FSS, Miljø.PROD_FSS })
public class PoaoTilgangServiceImpl implements PoaoTilgangService {
    private final PoaoTilgangClient klient;

    public PoaoTilgangServiceImpl(
            @Value("${tiltaksgjennomforing.poao-tilgang.url}") String poaoTilgangUrl,
            ClientConfigurationProperties clientConfigurationProperties,
            OAuth2AccessTokenService oAuth2AccessTokenService
    ) {
        ClientProperties clientProperties = clientConfigurationProperties.getRegistration().get("poao-tilgang");
        klient = PoaoTilgangCachedClient.createDefaultCacheClient(
                new PoaoTilgangHttpClient(
                    poaoTilgangUrl,
                    () -> oAuth2AccessTokenService.getAccessToken(clientProperties).getAccessToken(),
                    RestClient.baseClient()
                )
        );
    }

    public Optional<Tilgang> hentSkrivetilgang(UUID beslutterAzureUUID, Fnr fnr) {
        return Optional.ofNullable(hentSkrivetilgang(beslutterAzureUUID, fnr.asString()))
            .map(decision -> decision.isPermit()
                ? new Tilgang.Tillat()
                : new Tilgang.Avvis(Avslagskode.parse(decision), ((Decision.Deny) decision).getMessage())
            );
    }

    public Map<Fnr, Boolean> harSkrivetilganger(UUID beslutterAzureUUID, Set<Fnr> fnrSet) {
        return hentSkrivetilganger(beslutterAzureUUID, fnrSet)
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().isPermit()));
    }

    public Optional<Tilgangsattributter> hentTilgangsattributter(Fnr fnr) {
        return Optional.ofNullable(hentTilgangsattributter(fnr.asString()))
            .map(response -> new Tilgangsattributter(
                response.getKontor(),
                response.getSkjermet(),
                Optional.ofNullable(response.getDiskresjonskode())
                    .map(kode -> Diskresjonskode.parse(kode.name()))
                    .orElse(null)
            ));
    }

    private Map<Fnr, Decision> hentSkrivetilganger(UUID beslutterAzureUUID, Set<Fnr> fnrSet) {
       Map<UUID, Fnr> requestIdOgIdent = new HashMap<>();

        List<PolicyRequest> policyRequestList = fnrSet.stream()
            .map(fnr -> {
                UUID requestId = UUID.randomUUID();
                requestIdOgIdent.put(requestId, fnr);

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
                    policyResult -> requestIdOgIdent.get(policyResult.getRequestId()),
                    PolicyResult::getDecision
                ))
            )
            .orElse(Collections.emptyMap());
    }

    private Decision hentSkrivetilgang(UUID beslutterAzureUUID, String fnr) {
        return klient.evaluatePolicy(
            new NavAnsattTilgangTilEksternBrukerPolicyInput(
                beslutterAzureUUID,
                TilgangType.SKRIVE,
                fnr
            )
        ).get();
    }

    private TilgangsattributterResponse hentTilgangsattributter(String fnr) {
        return klient.hentTilgangsAttributter(fnr).get();
    }
}
