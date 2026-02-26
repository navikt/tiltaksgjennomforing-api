package no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet;

import io.getunleash.UnleashContext;
import io.getunleash.strategy.Strategy;
import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.enhet.entra.EntraproxyService;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class ByEnhetStrategy implements Strategy {

    static final String PARAM = "valgtEnhet";
    private final EntraproxyService entraproxyService;

    @Override
    public String getName() {
        return "byEnhet";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        Set<String> enhetFraToggle = Optional.ofNullable(parameters.get(PARAM))
            .map(enheterString -> Set.of(enheterString.split(",\\s?")))
            .orElse(Set.of());

        Optional<String> enhet = unleashContext.getByName("enhet");
        if (enhet.isPresent()) {
            return enhetFraToggle.contains(enhet.get());
        }

        return unleashContext.getUserId()
            .map(currentUserId -> !Collections.disjoint(enhetFraToggle, brukersEnheter(currentUserId)))
            .orElse(false);

    }

    private List<String> brukersEnheter(String currentUserId) {
        if (!NavIdent.erNavIdent(currentUserId)) {
            return List.of();
        }
        return entraproxyService.hentEnheterNavAnsattHarTilgangTil(new NavIdent(currentUserId))
            .stream()
            .map(NavEnhet::getVerdi)
            .collect(toList());
    }

}
