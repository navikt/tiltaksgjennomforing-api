package no.nav.tag.tiltaksgjennomforing.avtale;

public class ArbeidstreningStrategy extends BaseAvtaleInnholdStrategy {
    public ArbeidstreningStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    public void endre(EndreAvtale nyAvtale) {
        nyAvtale.getMaal().forEach(Maal::sjekkMaalLengde);
        nyAvtale.getOppgaver().forEach(Oppgave::sjekkOppgaveLengde);
        avtaleInnhold.getMaal().clear();
        avtaleInnhold.getMaal().addAll(nyAvtale.getMaal());
        avtaleInnhold.getMaal().forEach(m -> m.setAvtaleInnhold(avtaleInnhold));
        avtaleInnhold.getOppgaver().clear();
        avtaleInnhold.getOppgaver().addAll(nyAvtale.getOppgaver());
        avtaleInnhold.getOppgaver().forEach(o -> o.setAvtaleInnhold(avtaleInnhold));
        super.endre(nyAvtale);
    }

    @Override
    public boolean heleAvtaleUtfylt() {
        return super.heleAvtaleUtfylt()
                && !avtaleInnhold.getMaal().isEmpty()
                && !avtaleInnhold.getOppgaver().isEmpty();
    }
}
