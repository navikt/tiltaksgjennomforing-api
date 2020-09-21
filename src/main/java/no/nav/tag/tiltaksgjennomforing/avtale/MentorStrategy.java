package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetForLangMentorException;

import java.time.LocalDate;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erIkkeTomme;

public class MentorStrategy extends BaseAvtaleInnholdStrategy {

    private static final int MAKSIMALT_ANTALL_MÅNEDER_VARIGHET = 36;

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

    @Override
    public void sjekkOmVarighetErForLang(LocalDate startDato, LocalDate sluttDato) {
        if (startDato != null && sluttDato != null && startDato.plusMonths(MAKSIMALT_ANTALL_MÅNEDER_VARIGHET).isBefore(sluttDato)) {
            throw new VarighetForLangMentorException();
        }
    }
}
