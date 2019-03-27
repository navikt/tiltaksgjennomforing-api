package no.nav.tag.tiltaksgjennomforing.domene.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;
import no.nav.tag.tiltaksgjennomforing.domene.OpprettAvtale;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;

public class InnloggetNavAnsatt extends InnloggetBruker<NavIdent> {
    public InnloggetNavAnsatt(NavIdent identifikator) {
        super(identifikator);
    }

    public Avtale opprettAvtale(OpprettAvtale opprettAvtale) {
        return Avtale.nyAvtale(opprettAvtale, getIdentifikator());
    }

    @Override
    public boolean harTilgang(Avtale avtale) {
        if (avtale.getVeilederNavIdent().equals(getIdentifikator())) {
            return true;
        }
        return false;
    }
}
