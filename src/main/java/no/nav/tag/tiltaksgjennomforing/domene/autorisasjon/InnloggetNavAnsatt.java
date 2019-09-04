package no.nav.tag.tiltaksgjennomforing.domene.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.domene.*;

public class InnloggetNavAnsatt extends InnloggetBruker<NavIdent> {
    public InnloggetNavAnsatt(NavIdent identifikator) {
        super(identifikator);
    }

    public Avtale opprettAvtale(OpprettAvtale opprettAvtale) {
        return Avtale.nyAvtale(opprettAvtale, getIdentifikator());
    }

    @Override
    public Veileder avtalepart(Avtale avtale) {
        if (avtale.getVeilederNavIdent().equals(getIdentifikator())) {
            return new Veileder(getIdentifikator(), avtale);
        } else {
            return null;
        }
    }
}
