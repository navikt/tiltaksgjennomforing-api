package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public class Veileder extends Person<NavIdent> {
    public Veileder(NavIdent navIdent) {
        super(navIdent);
    }

    public Veileder(String navIdent) {
        this(new NavIdent(navIdent));
    }

    public Avtale opprettAvtale(OpprettAvtale opprettAvtale) {
        return Avtale.nyAvtale(opprettAvtale, getNavIdent());
    }

    public NavIdent getNavIdent() {
        return getIdentifikator();
    }
}
