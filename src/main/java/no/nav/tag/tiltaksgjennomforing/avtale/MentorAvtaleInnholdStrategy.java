package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold.Fields;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.MentorLonnstilskuddAvtaleBeregningStrategy;

import java.util.HashMap;
import java.util.Map;

public class MentorAvtaleInnholdStrategy extends LonnstilskuddAvtaleInnholdStrategy {
    private MentorLonnstilskuddAvtaleBeregningStrategy mentorBeregningStrategy;

    public MentorAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
        mentorBeregningStrategy = new MentorLonnstilskuddAvtaleBeregningStrategy();
    }

    @Override
    public void endre(EndreAvtale nyAvtale) {

        avtaleInnhold.setMentorFornavn(nyAvtale.getMentorFornavn());
        avtaleInnhold.setMentorEtternavn(nyAvtale.getMentorEtternavn());
        avtaleInnhold.setMentorOppgaver(nyAvtale.getMentorOppgaver());
        avtaleInnhold.setMentorAntallTimer(nyAvtale.getMentorAntallTimer());
        avtaleInnhold.setMentorTlf(nyAvtale.getMentorTlf());
        avtaleInnhold.setMentorTimelonn(nyAvtale.getMentorTimelonn());
        avtaleInnhold.setHarFamilietilknytning(nyAvtale.getHarFamilietilknytning());
        avtaleInnhold.setFamilietilknytningForklaring(nyAvtale.getFamilietilknytningForklaring());
        avtaleInnhold.setManedslonn(nyAvtale.getManedslonn());

        if (MentorTilskuddsperioderToggle.isEnabled()) {
            avtaleInnhold.setArbeidsgiverKontonummer(nyAvtale.getArbeidsgiverKontonummer());
            avtaleInnhold.setFeriepengesats(nyAvtale.getFeriepengesats());
            avtaleInnhold.setOtpSats(nyAvtale.getOtpSats());
            avtaleInnhold.setArbeidsgiveravgift(nyAvtale.getArbeidsgiveravgift());
        }

        super.endre(nyAvtale);
    }

    @Override
    public void regnUtTotalLonnstilskudd() {
        mentorBeregningStrategy.reberegnTotal(avtaleInnhold.getAvtale());
    }


    @Override
    public Map<String, Object> alleFelterSomMåFyllesUt() {
        var alleFelter = new HashMap<String, Object>();

        alleFelter.put(AvtaleInnhold.Fields.deltakerFornavn, avtaleInnhold.getDeltakerFornavn());
        alleFelter.put(AvtaleInnhold.Fields.deltakerEtternavn, avtaleInnhold.getDeltakerEtternavn());
        alleFelter.put(AvtaleInnhold.Fields.deltakerTlf, avtaleInnhold.getDeltakerTlf());
        alleFelter.put(AvtaleInnhold.Fields.bedriftNavn, avtaleInnhold.getBedriftNavn());
        alleFelter.put(AvtaleInnhold.Fields.arbeidsgiverFornavn, avtaleInnhold.getArbeidsgiverFornavn());
        alleFelter.put(AvtaleInnhold.Fields.arbeidsgiverEtternavn, avtaleInnhold.getArbeidsgiverEtternavn());
        alleFelter.put(AvtaleInnhold.Fields.arbeidsgiverTlf, avtaleInnhold.getArbeidsgiverTlf());

        alleFelter.put(AvtaleInnhold.Fields.veilederFornavn, avtaleInnhold.getVeilederFornavn());
        alleFelter.put(AvtaleInnhold.Fields.veilederEtternavn, avtaleInnhold.getVeilederEtternavn());
        alleFelter.put(AvtaleInnhold.Fields.veilederTlf, avtaleInnhold.getVeilederTlf());
        alleFelter.put(AvtaleInnhold.Fields.startDato, avtaleInnhold.getStartDato());
        alleFelter.put(AvtaleInnhold.Fields.sluttDato, avtaleInnhold.getSluttDato());
        alleFelter.put(AvtaleInnhold.Fields.oppfolging, avtaleInnhold.getOppfolging());
        alleFelter.put(AvtaleInnhold.Fields.tilrettelegging, avtaleInnhold.getTilrettelegging());


        alleFelter.put(AvtaleInnhold.Fields.mentorFornavn, avtaleInnhold.getMentorFornavn());
        alleFelter.put(AvtaleInnhold.Fields.mentorEtternavn, avtaleInnhold.getMentorEtternavn());
        alleFelter.put(AvtaleInnhold.Fields.mentorOppgaver, avtaleInnhold.getMentorOppgaver());
        alleFelter.put(AvtaleInnhold.Fields.mentorAntallTimer, avtaleInnhold.getMentorAntallTimer());
        alleFelter.put(AvtaleInnhold.Fields.mentorTimelonn, avtaleInnhold.getMentorTimelonn());
        alleFelter.put(Fields.mentorTlf, avtaleInnhold.getMentorTlf());

        if (MentorTilskuddsperioderToggle.isEnabled()) {
            alleFelter.put(AvtaleInnhold.Fields.arbeidsgiverKontonummer, avtaleInnhold.getArbeidsgiverKontonummer());
            alleFelter.put(AvtaleInnhold.Fields.feriepengesats, avtaleInnhold.getFeriepengesats());
            alleFelter.put(AvtaleInnhold.Fields.otpSats, avtaleInnhold.getOtpSats());
            alleFelter.put(AvtaleInnhold.Fields.arbeidsgiveravgift, avtaleInnhold.getArbeidsgiveravgift());
        }

        alleFelter.put(AvtaleInnhold.Fields.harFamilietilknytning, avtaleInnhold.getHarFamilietilknytning());
        if (avtaleInnhold.getHarFamilietilknytning() != null && avtaleInnhold.getHarFamilietilknytning()) {
            alleFelter.put(
                AvtaleInnhold.Fields.familietilknytningForklaring,
                avtaleInnhold.getFamilietilknytningForklaring()
            );
        }

        return alleFelter;
    }
}
