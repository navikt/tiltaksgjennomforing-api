package no.nav.tag.tiltaksgjennomforing.domene.autorisasjon;

import static no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService.NY_VEILEDERTILGANG;

import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.domene.*;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.integrasjon.veilarbabac.TilgangskontrollService;

@Slf4j
public class InnloggetNavAnsatt extends InnloggetBruker<NavIdent> {

    private final FeatureToggleService featureToggleService;
    private final TilgangskontrollService tilgangskontrollService;

    public InnloggetNavAnsatt(NavIdent identifikator, FeatureToggleService featureToggleService, TilgangskontrollService tilgangskontrollService) {
        super(identifikator);
        this.featureToggleService = featureToggleService;
        this.tilgangskontrollService = tilgangskontrollService;
    }

    public Avtale opprettAvtale(OpprettAvtale opprettAvtale) {
        return Avtale.nyAvtale(opprettAvtale, getIdentifikator());
    }

    @Override
    public Veileder avtalepart(Avtale avtale) {
        return featureToggleService.isEnabled(NY_VEILEDERTILGANG) || avtale.getVeilederNavIdent().equals(getIdentifikator()) 
                ? new Veileder(getIdentifikator(), avtale) 
                : null;
    }

    @Override
    public boolean harLeseTilgang(Avtale avtale)  {
        return featureToggleService.isEnabled(NY_VEILEDERTILGANG) 
                ? sjekkNyVeiledertilgang(avtale, f -> tilgangskontrollService.sjekkLesetilgangTilKandidat(this, f)) 
                : avtalepart(avtale) != null;
    }

    private boolean sjekkNyVeiledertilgang(Avtale avtale, Consumer<Fnr> callback) {
        try {
            callback.accept(avtale.getDeltakerFnr());
            return true;
        } catch (TilgangskontrollException e) {
            log.warn("Har ikke lesetilgang: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean harSkriveTilgang(Avtale avtale) {
        return featureToggleService.isEnabled(NY_VEILEDERTILGANG) 
                ? sjekkNyVeiledertilgang(avtale, f -> tilgangskontrollService.sjekkSkrivetilgangTilKandidat(this, f)) 
                : avtalepart(avtale) != null;
    }
}
