package no.nav.tag.tiltaksgjennomforing.domene;

public class Arbeidsgiver extends Avtalepart<Fnr> {
    public Arbeidsgiver(Fnr identifikator, Avtale avtale) {
        super(identifikator, avtale);
    }

    @Override
    public void endreGodkjenning(boolean godkjenning) {
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
