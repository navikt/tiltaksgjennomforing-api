package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.avtale.*;

import java.util.List;

import static java.util.Collections.emptyList;

public class InnloggetVeileder extends InnloggetBruker<NavIdent> {

    private final TilgangskontrollService tilgangskontrollService;

    public InnloggetVeileder(NavIdent identifikator, TilgangskontrollService tilgangskontrollService) {
        super(identifikator);
        this.tilgangskontrollService = tilgangskontrollService;
    }

    public Avtale opprettAvtale(OpprettAvtale opprettAvtale) {
        return Avtale.veilederOppretterAvtale(opprettAvtale, getIdentifikator());
    }

    @Override
    public Veileder avtalepart(Avtale avtale) {
        return new Veileder(getIdentifikator(), avtale);
    }

    @Override
    public boolean harLeseTilgang(Avtale avtale) {
        return tilgangskontrollService.harLesetilgangTilKandidat(this, avtale.getDeltakerFnr());
    }

    @Override
    public boolean harSkriveTilgang(Avtale avtale) {
        return tilgangskontrollService.harSkrivetilgangTilKandidat(this, avtale.getDeltakerFnr());
    }

    @Override
    public boolean erNavAnsatt() {
        return true;
    }

    @Override
    public Avtalerolle rolle() {
        return Avtalerolle.VEILEDER;
    }

    @Override
    List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        if (queryParametre.getVeilederNavIdent() != null) {
            return avtaleRepository.findAllByVeilederNavIdent(queryParametre.getVeilederNavIdent());
        } else if (queryParametre.getDeltakerFnr() != null) {
            return avtaleRepository.findAllByDeltakerFnr(queryParametre.getDeltakerFnr());
        } else if (queryParametre.getBedriftNr() != null) {
            return avtaleRepository.findAllByBedriftNrIn(List.of(queryParametre.getBedriftNr()));
        } else if (queryParametre.getErUfordelt() == true) {
            return avtaleRepository.findAllByVeilederNavIdent(null);
        } else {
            return emptyList();
        }
    }
}
