package no.nav.tag.tiltaksgjennomforing.domene.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.domene.*;

import java.util.UUID;

public class InnloggetNavAnsatt extends InnloggetBruker<NavIdent> {
    public InnloggetNavAnsatt(NavIdent identifikator) {
        super(identifikator);
    }

    public Avtale opprettAvtale(OpprettAvtale opprettAvtale) {
        return Avtale.nyAvtale(opprettAvtale, getIdentifikator());
    }
    public Avtale opprettAvtale(OpprettAvtale opprettAvtale, UUID sisteVersjonID, UUID baseAvtaleID, int godkjentVersjon)
    {
        return Avtale.nyAvtaleVersjon(opprettAvtale,getIdentifikator(),sisteVersjonID,baseAvtaleID,godkjentVersjon);
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
