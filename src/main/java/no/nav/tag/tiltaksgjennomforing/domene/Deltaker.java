package no.nav.tag.tiltaksgjennomforing.domene;

public class Deltaker extends Avtalepart<Fnr> {

    public Deltaker(Fnr identifikator, Avtale avtale) {
        super(identifikator, avtale);
    }

    @Override
    public void godkjennForAvtalepart() {
        avtale.godkjennForDeltaker();
    }

    @Override
    public boolean kanEndreAvtale() {
        return false;
    }

    @Override
    boolean kanOppheveGodkjenninger() {
        return false;
    }

    @Override
    public Rolle rolle() {
        return Rolle.DELTAKER;
    }
}
