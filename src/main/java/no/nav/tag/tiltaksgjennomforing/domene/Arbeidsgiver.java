package no.nav.tag.tiltaksgjennomforing.domene;

public class Arbeidsgiver extends Avtalepart<Fnr> {
    public Arbeidsgiver(Fnr identifikator, Avtale avtale) {
        super(identifikator, avtale);
    }

    @Override
    public void godkjennForAvtalepart() {
        avtale.godkjennForArbeidsgiver();
    }

    @Override
    public boolean kanEndreAvtale() {
        return true;
    }

    @Override
    boolean kanOppheveGodkjenninger() {
        return !avtale.isGodkjentAvVeileder();
    }

    @Override
    public Rolle rolle() {
        return Rolle.ARBEIDSGIVER;
    }
}
