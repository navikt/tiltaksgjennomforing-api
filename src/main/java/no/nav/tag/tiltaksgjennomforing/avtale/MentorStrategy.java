package no.nav.tag.tiltaksgjennomforing.avtale;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erIkkeTomme;

public class MentorStrategy extends BaseAvtaleInnholdStrategy {

    public MentorStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    public void endre(EndreAvtale nyAvtale) {
        avtaleInnhold.setMentorFornavn(nyAvtale.getMentorFornavn());
        avtaleInnhold.setMentorEtternavn(nyAvtale.getMentorEtternavn());
        avtaleInnhold.setMentorOppgaver(nyAvtale.getMentorOppgaver());
        avtaleInnhold.setMentorAntallTimer(nyAvtale.getMentorAntallTimer());
        avtaleInnhold.setMentorTimelonn(nyAvtale.getMentorTimelonn());
        super.endre(nyAvtale);
    }

    @Override
    public boolean erAltUtfylt() {
        return super.erAltUtfylt() && erIkkeTomme(
                avtaleInnhold.getMentorFornavn(),
                avtaleInnhold.getMentorEtternavn(),
                avtaleInnhold.getMentorOppgaver(),
                avtaleInnhold.getMentorAntallTimer(),
                avtaleInnhold.getMentorTimelonn()
        );
    }
}
