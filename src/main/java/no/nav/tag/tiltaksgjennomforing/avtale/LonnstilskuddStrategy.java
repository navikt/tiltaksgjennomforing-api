package no.nav.tag.tiltaksgjennomforing.avtale;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erIkkeTomme;

import java.math.BigDecimal;
import org.apache.commons.lang3.StringUtils;

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
        Integer obligTjenestepensjon = getBeregnetOptBelop(nyAvtale.getManedslonn(), feriepengerBelop);
        Integer arbeidsgiveravgiftBelop = getArbeidsgiverAvgift(avtaleInnhold.getManedslonn(), feriepengerBelop, obligTjenestepensjon,
            nyAvtale.getArbeidsgiveravgift());
        Integer sumLonnsutgifter = getSumLonnsutgifter(nyAvtale.getManedslonn(), feriepengerBelop, obligTjenestepensjon, arbeidsgiveravgiftBelop);

        avtaleInnhold.setFeriepengerBelop(feriepengerBelop);
        avtaleInnhold.setOtpBelop(obligTjenestepensjon);
        avtaleInnhold.setArbeidsgiveravgiftBelop(arbeidsgiveravgiftBelop);
        avtaleInnhold.setSumLonnsutgifter(sumLonnsutgifter);
        avtaleInnhold.setSumLonnstilskudd(getSumLonnsTilskudd(sumLonnsutgifter, nyAvtale.getLonnstilskuddProsent()));
    }

    private Integer getSumLonnsTilskudd(Integer sumLonnsutgifter, Integer lonnstilskuddProsent) {
        if (sumLonnsutgifter == null) {
            return null;
        }
        double lonnstilskuddLonnDecimal = lonnstilskuddProsent != null ? (lonnstilskuddProsent.doubleValue() / 100) : 0;
        return (int) Math.round(sumLonnsutgifter * lonnstilskuddLonnDecimal);
    }

    private Integer getSumLonnsutgifter(Integer manedslonn, Integer feriepengerBelop, Integer obligTjenestepensjon, Integer arbeidsgiveravgiftBelop) {
        if (feriepengerBelop != null && obligTjenestepensjon != null && arbeidsgiveravgiftBelop != null) {
            return manedslonn + feriepengerBelop + obligTjenestepensjon + arbeidsgiveravgiftBelop;
        }
        return null;
    }

    private Integer getArbeidsgiverAvgift(Integer manedslonn, Integer feriepengerBelop, Integer obligTjenestepensjon, BigDecimal arbeidsgiveravgift) {
        if (manedslonn != null && feriepengerBelop != null && obligTjenestepensjon != null && arbeidsgiveravgift != null) {
            return (int) Math.round((manedslonn + feriepengerBelop + obligTjenestepensjon) * (arbeidsgiveravgift.doubleValue() / 100));
        }
        return null;
    }

    private Integer getBeregnetOptBelop(Integer manedslonn, Integer feriepenger) {
        if (manedslonn != null && feriepenger != null) {
            return (int) ((manedslonn + feriepenger) * 0.02);
        }
        return null;
    }

    private Integer getFeriepengerBelop(BigDecimal feriepengersats, Integer manedslonn) {
        if (feriepengersats != null && manedslonn != null) {
            return (int) ((feriepengersats.doubleValue() / 100) * manedslonn);
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
