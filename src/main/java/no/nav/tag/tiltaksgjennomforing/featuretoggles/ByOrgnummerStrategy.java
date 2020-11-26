package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import lombok.Data;
import no.finn.unleash.UnleashContext;
import no.finn.unleash.strategy.Strategy;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring.AltinnTilgangsstyringService;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
 @Component
public class ByOrgnummerStrategy implements Strategy {

     private final String UNLEASH_PARAMETER_ORGNUMRE = "orgnumre";
     private final AltinnTilgangsstyringService altinnTilgangsstyringService;

    @Override
    public String getName() {
        return "byOrgnummer";
    }

    @Override
    public boolean isEnabled(Map<String, String> map) {
        return false;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        if(unleashContext.getUserId().isPresent()) {
            return tilhørerOrganisasjon(new Fnr(unleashContext.getUserId().get()), parameters);
        }
        return false;
    }

     private boolean tilhørerOrganisasjon(Fnr fnr, Map<String, String> parameters){
         Set<AltinnReportee> altinnOrganisasjoner = altinnTilgangsstyringService.hentAltinnOrganisasjoner(fnr);
         List<String> altinnOrgnr = altinnOrganisasjoner.stream().map(org -> org.getOrganizationNumber()).collect(Collectors.toList());
         List<String> matchPåOrgnummer = altinnOrgnr.stream().filter(parameters::containsValue).collect(Collectors.toList());
         return !matchPåOrgnummer.isEmpty();
     }

}
