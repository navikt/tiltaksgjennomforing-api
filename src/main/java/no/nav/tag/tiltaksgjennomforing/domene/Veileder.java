package no.nav.tag.tiltaksgjennomforing.domene;

public class Veileder extends Avtalepart<NavIdent> {

    public Veileder(NavIdent identifikator, Avtale avtale) {
        super(identifikator, avtale);
    }

    @Override
    public void endreGodkjenning(boolean godkjenning) {
        avtale.endreVeiledersGodkjennelse(godkjenning);
    }

    @Override
    public boolean kanEndreAvtale() {
        return true;
    }

    @Override
    public Rolle rolle() {
        return Rolle.VEILEDER;
    }
}
