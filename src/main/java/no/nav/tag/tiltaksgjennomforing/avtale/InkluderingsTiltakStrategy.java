package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.HashMap;
import java.util.Map;

public class InkluderingsTiltakStrategy extends BaseAvtaleInnholdStrategy {

    public InkluderingsTiltakStrategy(AvtaleInnhold avtaleInnhold){
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
        var alleFelter = new HashMap<String, Object>();
        alleFelter.putAll(super.alleFelterSomMåFyllesUt());
        alleFelter.put(AvtaleInnhold.Fields.mentorFornavn, avtaleInnhold.getMentorFornavn());
        alleFelter.put(AvtaleInnhold.Fields.mentorEtternavn, avtaleInnhold.getMentorEtternavn());
        alleFelter.put(AvtaleInnhold.Fields.mentorOppgaver, avtaleInnhold.getMentorOppgaver());
        alleFelter.put(AvtaleInnhold.Fields.mentorAntallTimer, avtaleInnhold.getMentorAntallTimer());
        alleFelter.put(AvtaleInnhold.Fields.mentorTimelonn, avtaleInnhold.getMentorTimelonn());
        return alleFelter;
    }
}
