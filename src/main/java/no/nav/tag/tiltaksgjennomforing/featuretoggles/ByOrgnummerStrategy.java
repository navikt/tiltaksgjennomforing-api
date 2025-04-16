package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import io.getunleash.UnleashContext;
import io.getunleash.strategy.Strategy;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringService;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class ByOrgnummerStrategy implements Strategy {

    static final String UNLEASH_PARAMETER_ORGNUMRE = "orgnumre";
    private final AltinnTilgangsstyringService altinnTilgangsstyringService;

    public ByOrgnummerStrategy(AltinnTilgangsstyringService altinnTilgangsstyringService) {
        this.altinnTilgangsstyringService = altinnTilgangsstyringService;
    }

    @Override
    public String getName() {
        return "byOrgnummer";
    }


    @Override
    public boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        return unleashContext.getUserId()
                .flatMap(currentUserId -> Optional.ofNullable(parameters.get(UNLEASH_PARAMETER_ORGNUMRE))
                        .map(enheterOrg -> Set.of(enheterOrg.split(",\\s?")))
                        .map(enabledeOrg -> !Collections.disjoint(enabledeOrg, brukersOrganisasjoner(currentUserId))))
                .orElse(false);
    }

    private List<String> brukersOrganisasjoner(String currentUserId) {
        if (NavIdent.erNavIdent(currentUserId)) {
            return List.of();
        }
        try {
            //TODO: Fungerer pt. ikke. bruker kun dummy hentArbeidsgivrtoken.
            Set<AltinnReportee> altinnOrganisasjoner = altinnTilgangsstyringService.hentAltinnOrganisasjoner(new Fnr(currentUserId), () -> "");
            return altinnOrganisasjoner.stream().map(AltinnReportee::getOrganizationNumber).toList();
        } catch (Exception e) {
            log.error("Feil ved oppslag på brukers organisasjoner i Altinn: {}", e.getMessage());
            return List.of();
        }
    }
}
