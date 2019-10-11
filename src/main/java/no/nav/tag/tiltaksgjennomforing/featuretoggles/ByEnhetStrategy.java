package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import no.finn.unleash.UnleashContext;
import no.finn.unleash.strategy.Strategy;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.pilottilgang.AxsysService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ByEnhetStrategy implements Strategy {

    static final String PARAM = "valgtEnhet";
    private final AxsysService axsysService;

    @Override
    public String getName() {
        return "byEnhet";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        return unleashContext.getUserId()
                .flatMap(currentUserId -> Optional.ofNullable(parameters.get(PARAM))
                        .map(enheterString -> Set.of(enheterString.split(",\\s?")))
                        .map(enabledeEnheter -> !Collections.disjoint(enabledeEnheter, brukersEnheter(currentUserId))))
                .orElse(false);

    }

    private List<String> brukersEnheter(String currentUserId) {
        return axsysService.hentEnheterVeilederHarTilgangTil(new NavIdent(currentUserId)).stream()
                .map(enhet -> enhet.getVerdi()).collect(toList());
    }

}
