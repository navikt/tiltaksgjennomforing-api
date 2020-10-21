package no.nav.tag.tiltaksgjennomforing.avtale;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erIkkeTomme;

public class LonnstilskuddStrategy extends BaseAvtaleInnholdStrategy {
    public LonnstilskuddStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
    }

    @Override
    public void endre(EndreAvtale nyAvtale) {
        avtaleInnhold.setArbeidsgiverKontonummer(nyAvtale.getArbeidsgiverKontonummer());
        avtaleInnhold.setLonnstilskuddProsent(nyAvtale.getLonnstilskuddProsent());
        avtaleInnhold.setManedslonn(nyAvtale.getManedslonn());
        avtaleInnhold.setFeriepengesats(nyAvtale.getFeriepengesats());
        avtaleInnhold.setArbeidsgiveravgift(nyAvtale.getArbeidsgiveravgift());
        avtaleInnhold.setHarFamilietilknytning(nyAvtale.getHarFamilietilknytning());
        avtaleInnhold.setFamilietilknytningForklaring(nyAvtale.getFamilietilknytningForklaring());
        avtaleInnhold.setStillingstype(nyAvtale.getStillingstype());
        avtaleInnhold.setStillingstittel(nyAvtale.getStillingstittel());
        avtaleInnhold.setStillingStyrk08(nyAvtale.getStillingStyrk08());
        avtaleInnhold.setStillingKonseptId(nyAvtale.getStillingKonseptId());
        regnUtTotalLonnstilskudd(nyAvtale);
        super.endre(nyAvtale);
    }

    private void regnUtTotalLonnstilskudd(EndreAvtale nyAvtale) {
        Integer feriepengerBelop = getFeriepengerBelop(nyAvtale.getFeriepengesats(), nyAvtale.getManedslonn());
        Integer obligTjenestepensjon = getBeregnetOtpBelop(nyAvtale.getManedslonn(), feriepengerBelop);
        Integer arbeidsgiveravgiftBelop = getArbeidsgiverAvgift(avtaleInnhold.getManedslonn(), feriepengerBelop, obligTjenestepensjon,
            nyAvtale.getArbeidsgiveravgift());
        Integer sumLonnsutgifter = getSumLonnsutgifter(nyAvtale.getManedslonn(), feriepengerBelop, obligTjenestepensjon, arbeidsgiveravgiftBelop);
        Integer månedslønnFullStilling = getLønnVedFullStilling(nyAvtale.getSumLonnsutgifter(), nyAvtale.getStillingprosent());

        avtaleInnhold.setFeriepengerBelop(feriepengerBelop);
        avtaleInnhold.setOtpBelop(obligTjenestepensjon);
        avtaleInnhold.setArbeidsgiveravgiftBelop(arbeidsgiveravgiftBelop);
        avtaleInnhold.setSumLonnsutgifter(sumLonnsutgifter);
        avtaleInnhold.setSumLonnstilskudd(getSumLonnsTilskudd(sumLonnsutgifter, nyAvtale.getLonnstilskuddProsent()));
        avtaleInnhold.setManedslonn100pst(månedslønnFullStilling);
    }

    private Integer getLønnVedFullStilling(Integer sumUtgifter, Integer stillingsProsent){
        if(sumUtgifter == null || stillingsProsent == null){
            return null;
        }
        return (sumUtgifter * 100) / stillingsProsent;
    }

    private Integer getSumLonnsTilskudd(Integer sumLonnsutgifter, Integer lonnstilskuddProsent) {
        if (sumLonnsutgifter == null || lonnstilskuddProsent == null) {
            return null;
        }
        double lonnstilskuddProsentSomDecimal = lonnstilskuddProsent != null ? (lonnstilskuddProsent.doubleValue() / 100) : 0;
        return (int) Math.round(sumLonnsutgifter * lonnstilskuddProsentSomDecimal);
    }

    private Integer getSumLonnsutgifter(Integer manedslonn, Integer feriepengerBelop, Integer obligTjenestepensjon, Integer arbeidsgiveravgiftBelop) {
        if (erIkkeTomme(feriepengerBelop, obligTjenestepensjon, arbeidsgiveravgiftBelop)) {
            return manedslonn + feriepengerBelop + obligTjenestepensjon + arbeidsgiveravgiftBelop;
        }
        return null;
    }

    private Integer getArbeidsgiverAvgift(Integer manedslonn, Integer feriepengerBelop, Integer obligTjenestepensjon, BigDecimal arbeidsgiveravgift) {
        if (erIkkeTomme(manedslonn, feriepengerBelop, obligTjenestepensjon, arbeidsgiveravgift)) {
            return (int) Math.round((manedslonn + feriepengerBelop + obligTjenestepensjon) * (arbeidsgiveravgift.doubleValue()));
        }
        return null;
    }

    private Integer getBeregnetOtpBelop(Integer manedslonn, Integer feriepenger) {
        if (erIkkeTomme(manedslonn, feriepenger)) {
            double OBLIG_TJENESTEPENSJON_PROSENT_SATS = 0.02;
            return (int) Math.round((manedslonn + feriepenger) * OBLIG_TJENESTEPENSJON_PROSENT_SATS);
        }
        return null;
    }

    private Integer getFeriepengerBelop(BigDecimal feriepengersats, Integer manedslonn) {
        if (erIkkeTomme(feriepengersats, manedslonn)) {
            return (int) Math.round((feriepengersats.doubleValue()) * manedslonn);
        }
        return null;
    }

    private boolean erFamiletilknytningForklaringFylltUtHvisDetTrengs() {
        if (avtaleInnhold.getHarFamilietilknytning()) {
            return StringUtils.isNotBlank(avtaleInnhold.getFamilietilknytningForklaring());
        } else {
            return true;
        }
    }

    @Override
    public boolean erAltUtfylt() {
        return super.erAltUtfylt() && erIkkeTomme(
                avtaleInnhold.getArbeidsgiverKontonummer(),
                avtaleInnhold.getStillingstittel(),
                avtaleInnhold.getArbeidsoppgaver(),
                avtaleInnhold.getLonnstilskuddProsent(),
                avtaleInnhold.getManedslonn(),
                avtaleInnhold.getFeriepengesats(),
                avtaleInnhold.getArbeidsgiveravgift(),
                avtaleInnhold.getHarFamilietilknytning(),
                avtaleInnhold.getStillingstype()
        ) && erFamiletilknytningForklaringFylltUtHvisDetTrengs();
    }
}
