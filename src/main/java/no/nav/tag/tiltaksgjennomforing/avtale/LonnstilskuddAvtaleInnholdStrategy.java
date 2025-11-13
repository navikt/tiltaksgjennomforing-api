package no.nav.tag.tiltaksgjennomforing.avtale;

import no.bekk.bekkopen.banking.KidnummerValidator;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.EndreTilskuddsberegning;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.GenerellLonnstilskuddAvtaleBeregningStrategy;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.BeregningStrategy;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class LonnstilskuddAvtaleInnholdStrategy extends BaseAvtaleInnholdStrategy {

    private BeregningStrategy beregningStrategy;

    public LonnstilskuddAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
        beregningStrategy = new GenerellLonnstilskuddAvtaleBeregningStrategy();
    }

    @Override
    public void endre(EndreAvtale nyAvtale) {
        if (nyAvtale.getOtpSats() != null && (nyAvtale.getOtpSats() > 0.3 || nyAvtale.getOtpSats() < 0.0)) {
            throw new FeilkodeException(Feilkode.FEIL_OTP_SATS);
        }
        if (nyAvtale.getArbeidsgiverKid() != null && !KidnummerValidator.isValid(nyAvtale.getArbeidsgiverKid())) {
            throw new FeilkodeException(Feilkode.FEIL_KID_NUMMER);
        }

        avtaleInnhold.setArbeidsgiverKontonummer(nyAvtale.getArbeidsgiverKontonummer());
        avtaleInnhold.setArbeidsgiverKid(nyAvtale.getArbeidsgiverKid());
        avtaleInnhold.setManedslonn(nyAvtale.getManedslonn());
        avtaleInnhold.setFeriepengesats(nyAvtale.getFeriepengesats());
        avtaleInnhold.setArbeidsgiveravgift(nyAvtale.getArbeidsgiveravgift());
        avtaleInnhold.setHarFamilietilknytning(nyAvtale.getHarFamilietilknytning());
        avtaleInnhold.setFamilietilknytningForklaring(nyAvtale.getFamilietilknytningForklaring());
        avtaleInnhold.setStillingstype(nyAvtale.getStillingstype());
        avtaleInnhold.setStillingstittel(nyAvtale.getStillingstittel());
        avtaleInnhold.setStillingStyrk08(nyAvtale.getStillingStyrk08());
        avtaleInnhold.setStillingKonseptId(nyAvtale.getStillingKonseptId());
        avtaleInnhold.setOtpSats(nyAvtale.getOtpSats());
        avtaleInnhold.setRefusjonKontaktperson(nyAvtale.getRefusjonKontaktperson());
        super.endre(nyAvtale);
        regnUtTotalLonnstilskudd();
    }

    @Override
    public void endreTilskuddsberegning(EndreTilskuddsberegning endreTilskuddsberegning) {
        avtaleInnhold.setArbeidsgiveravgift(endreTilskuddsberegning.getArbeidsgiveravgift());
        avtaleInnhold.setOtpSats(endreTilskuddsberegning.getOtpSats());
        avtaleInnhold.setManedslonn(endreTilskuddsberegning.getManedslonn());
        avtaleInnhold.setFeriepengesats(endreTilskuddsberegning.getFeriepengesats());
       regnUtTotalLonnstilskudd();
    }

    @Override
    public void regnUtTotalLonnstilskudd() {
        beregningStrategy.reberegnTotal(avtaleInnhold.getAvtale());
    }

    @Override
    public Map<String, Object> alleFelterSomMåFyllesUt() {
        HashMap<String, Object> alleFelter = new HashMap<>();
        alleFelter.putAll(super.alleFelterSomMåFyllesUt());
        alleFelter.put(AvtaleInnhold.Fields.stillingstittel, avtaleInnhold.getStillingstittel());
        alleFelter.put(AvtaleInnhold.Fields.stillingprosent, avtaleInnhold.getStillingprosent());
        alleFelter.put(AvtaleInnhold.Fields.arbeidsoppgaver, avtaleInnhold.getArbeidsoppgaver());
        alleFelter.put(AvtaleInnhold.Fields.arbeidsgiverKontonummer, avtaleInnhold.getArbeidsgiverKontonummer());
        alleFelter.put(AvtaleInnhold.Fields.manedslonn, avtaleInnhold.getManedslonn());
        alleFelter.put(AvtaleInnhold.Fields.feriepengesats, avtaleInnhold.getFeriepengesats());
        alleFelter.put(AvtaleInnhold.Fields.otpSats, avtaleInnhold.getOtpSats());
        alleFelter.put(AvtaleInnhold.Fields.arbeidsgiveravgift, avtaleInnhold.getArbeidsgiveravgift());
        alleFelter.put(AvtaleInnhold.Fields.harFamilietilknytning, avtaleInnhold.getHarFamilietilknytning());
        alleFelter.put(AvtaleInnhold.Fields.stillingstype, avtaleInnhold.getStillingstype());
        alleFelter.put(AvtaleInnhold.Fields.antallDagerPerUke, avtaleInnhold.getAntallDagerPerUke());
        if (avtaleInnhold.getHarFamilietilknytning() != null && avtaleInnhold.getHarFamilietilknytning()) {
            alleFelter.put(AvtaleInnhold.Fields.familietilknytningForklaring, avtaleInnhold.getFamilietilknytningForklaring());
        }
        return alleFelter;
    }

    @Override
    public void endreSluttDato(LocalDate nySluttDato) {
        super.endreSluttDato(nySluttDato);
        regnUtTotalLonnstilskudd();
    }
}
