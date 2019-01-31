package no.nav.tag.tiltaksgjennomforing.domene;

public class Deltaker extends Avtalepart<Fnr> {

    public Deltaker(Fnr identifikator) {
        super(identifikator);
    }

    @Override
    public void endreGodkjenning(Avtale avtale, boolean godkjenning) {
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
