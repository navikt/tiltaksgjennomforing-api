package no.nav.tag.tiltaksgjennomforing.avtale;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erIkkeTomme;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

public class LonnstilskuddStrategy extends BaseAvtaleInnholdStrategy {
    public LonnstilskuddStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    public void endre(EndreAvtale nyAvtale) {
        if (nyAvtale.getOtpSats() != null && (nyAvtale.getOtpSats() > 0.3 || nyAvtale.getOtpSats() < 0.0)) {
            throw new FeilkodeException(Feilkode.FEIL_OTP_SATS);
        }
        avtaleInnhold.setArbeidsgiverKontonummer(nyAvtale.getArbeidsgiverKontonummer());
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
        BigDecimal feriepengerBelop = getFeriepengerBelop(avtaleInnhold.getFeriepengesats(), avtaleInnhold.getManedslonn());
        BigDecimal obligTjenestepensjon = getBeregnetOtpBelop(toBigDecimal(avtaleInnhold.getOtpSats()), avtaleInnhold.getManedslonn(), feriepengerBelop);
        BigDecimal arbeidsgiveravgiftBelop = getArbeidsgiverAvgift(avtaleInnhold.getManedslonn(), feriepengerBelop, obligTjenestepensjon,
                avtaleInnhold.getArbeidsgiveravgift());
        Integer sumLonnsutgifter = getSumLonnsutgifter(avtaleInnhold.getManedslonn(), feriepengerBelop, obligTjenestepensjon, arbeidsgiveravgiftBelop);
        Integer sumlønnTilskudd = getSumLonnsTilskudd(sumLonnsutgifter, avtaleInnhold.getLonnstilskuddProsent());
        Integer månedslønnFullStilling = getLønnVedFullStilling(sumLonnsutgifter, avtaleInnhold.getStillingprosent());
        avtaleInnhold.setFeriepengerBelop(convertBigDecimalToInt(feriepengerBelop));
        avtaleInnhold.setOtpBelop(convertBigDecimalToInt(obligTjenestepensjon));
        avtaleInnhold.setArbeidsgiveravgiftBelop(convertBigDecimalToInt(arbeidsgiveravgiftBelop));
        avtaleInnhold.setSumLonnsutgifter(sumLonnsutgifter);
        avtaleInnhold.setSumLonnstilskudd(sumlønnTilskudd);
        avtaleInnhold.setManedslonn100pst(månedslønnFullStilling);
    }

    private BigDecimal toBigDecimal (Double value) {
        if(value == null){
            return null;
        }
        return BigDecimal.valueOf(value);
    }

    private Integer getLønnVedFullStilling(Integer sumUtgifter, Integer stillingsProsent) {
        if (sumUtgifter == null || stillingsProsent == null || stillingsProsent == 0) {
            return null;
        }
        return (sumUtgifter * 100) / stillingsProsent;
    }

    Integer getSumLonnsTilskudd(Integer sumLonnsutgifter, Integer lonnstilskuddProsent) {
        if (sumLonnsutgifter == null || lonnstilskuddProsent == null) {
            return null;
        }
        double lonnstilskuddProsentSomDecimal = lonnstilskuddProsent != null ? (lonnstilskuddProsent.doubleValue() / 100) : 0;
        return (int) Math.round(sumLonnsutgifter * lonnstilskuddProsentSomDecimal);
    }

    private Integer convertBigDecimalToInt(BigDecimal value){
        return value == null ? null : value.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private Integer getSumLonnsutgifter(Integer manedslonn, BigDecimal feriepengerBelop, BigDecimal obligTjenestepensjon, BigDecimal arbeidsgiveravgiftBelop) {
        if (erIkkeTomme(feriepengerBelop, obligTjenestepensjon, arbeidsgiveravgiftBelop)) {
            return manedslonn + convertBigDecimalToInt(feriepengerBelop.add(obligTjenestepensjon).add(arbeidsgiveravgiftBelop));
        }
        return null;
    }

    private BigDecimal getArbeidsgiverAvgift(Integer manedslonn, BigDecimal feriepengerBelop, BigDecimal obligTjenestepensjon, BigDecimal arbeidsgiveravgift) {
        if (erIkkeTomme(manedslonn, feriepengerBelop, obligTjenestepensjon, arbeidsgiveravgift)) {
            return   arbeidsgiveravgift.multiply(BigDecimal.valueOf(manedslonn).add(feriepengerBelop).add(obligTjenestepensjon));
        }
        return null;
    }

    private BigDecimal getBeregnetOtpBelop(BigDecimal optSats, Integer manedslonn, BigDecimal feriepenger) {
        if (erIkkeTomme(optSats, manedslonn, feriepenger)) {
            return (optSats.multiply(BigDecimal.valueOf(manedslonn).add(feriepenger)));
        }
        return null;
    }

    private BigDecimal getFeriepengerBelop(BigDecimal feriepengersats, Integer manedslonn) {
        if (erIkkeTomme(feriepengersats, manedslonn)) {
            return (feriepengersats.multiply(BigDecimal.valueOf(manedslonn)));
        }
        return null;
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
