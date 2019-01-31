package no.nav.tag.tiltaksgjennomforing.domene;

public class Deltaker extends Avtalepart<Fnr> {

    public Deltaker(Fnr identifikator, Avtale avtale) {
        super(identifikator, avtale);
    }

    @Override
    public void endreGodkjenning(boolean godkjenning) {
        avtale.endreDeltakersGodkjennelse(godkjenning);
    }

    @Override
    public boolean kanEndreAvtale() {
        return false;
    }

    @Override
    public Rolle rolle() {
        return Rolle.DELTAKER;
    }
}
