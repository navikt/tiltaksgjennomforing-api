package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetForLangMentorException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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
    public Map<String, Object> alleFelterSomMåFyllesUt() {
        HashMap<String, Object> alleFelter = new HashMap<>();
        alleFelter.putAll(super.alleFelterSomMåFyllesUt());
        alleFelter.put(AvtaleInnhold.Fields.mentorFornavn, avtaleInnhold.getMentorFornavn());
        alleFelter.put(AvtaleInnhold.Fields.mentorFornavn, avtaleInnhold.getMentorEtternavn());
        alleFelter.put(AvtaleInnhold.Fields.mentorFornavn, avtaleInnhold.getMentorOppgaver());
        alleFelter.put(AvtaleInnhold.Fields.mentorFornavn, avtaleInnhold.getMentorAntallTimer());
        alleFelter.put(AvtaleInnhold.Fields.mentorFornavn, avtaleInnhold.getMentorTimelonn());
        return alleFelter;
    }

    @Override
    public void sjekkOmVarighetErForLang(LocalDate startDato, LocalDate sluttDato) {
        if (startDato != null && sluttDato != null && startDato.plusMonths(MAKSIMALT_ANTALL_MÅNEDER_VARIGHET).isBefore(sluttDato)) {
            throw new VarighetForLangMentorException();
        }
    }
}
