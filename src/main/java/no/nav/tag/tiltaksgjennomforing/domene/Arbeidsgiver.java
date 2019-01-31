package no.nav.tag.tiltaksgjennomforing.domene;

public class Arbeidsgiver extends Avtalepart<Fnr> {
    public Arbeidsgiver(Fnr fnr) {
        super(fnr);
    }

    @Override
    public void endreGodkjenning(Avtale avtale, boolean godkjenning) {
        avtale.endreArbeidsgiversGodkjennelse(godkjenning);
    }

    @Override
    public boolean kanEndreAvtale() {
        return true;
    }

    @Override
    public Rolle rolle() {
        return Rolle.ARBEIDSGIVER;
    }
}
