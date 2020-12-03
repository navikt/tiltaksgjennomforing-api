package no.nav.tag.tiltaksgjennomforing.avtale;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erIkkeTomme;

import java.math.BigDecimal;
import java.util.List;
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
        avtaleInnhold.setOtpSats(getOtpSats(nyAvtale));
        regnUtTotalLonnstilskudd(nyAvtale);
        super.endre(nyAvtale);
    }


    private void regnUtTotalLonnstilskudd(EndreAvtale nyAvtale) {
        Integer feriepengerBelop = getFeriepengerBelop(nyAvtale.getFeriepengesats(), nyAvtale.getManedslonn());
        Integer obligTjenestepensjon = getBeregnetOtpBelop(nyAvtale.getOtpSats(), nyAvtale.getManedslonn(), feriepengerBelop);
        Integer arbeidsgiveravgiftBelop = getArbeidsgiverAvgift(avtaleInnhold.getManedslonn(), feriepengerBelop, obligTjenestepensjon,
            nyAvtale.getArbeidsgiveravgift());
        Integer sumLonnsutgifter = getSumLonnsutgifter(nyAvtale.getManedslonn(), feriepengerBelop, obligTjenestepensjon, arbeidsgiveravgiftBelop);
        Integer sumlønnTilskudd = getSumLonnsTilskudd(sumLonnsutgifter, nyAvtale.getLonnstilskuddProsent());
        Integer månedslønnFullStilling = getLønnVedFullStilling(sumLonnsutgifter, nyAvtale.getStillingprosent());
        avtaleInnhold.setFeriepengerBelop(feriepengerBelop);
        avtaleInnhold.setOtpBelop(obligTjenestepensjon);
        avtaleInnhold.setArbeidsgiveravgiftBelop(arbeidsgiveravgiftBelop);
        avtaleInnhold.setSumLonnsutgifter(sumLonnsutgifter);
        avtaleInnhold.setSumLonnstilskudd(sumlønnTilskudd);
        avtaleInnhold.setManedslonn100pst(månedslønnFullStilling);
        regnUtrefusjonsperioder(nyAvtale);
    }

    private void regnUtrefusjonsperioder(EndreAvtale nyAvtale) {
        avtaleInnhold.getTilskuddPeriode().clear();
        if(harAllePåkrevdeFeltForRegneUtTilskuddsPeriode(nyAvtale)) {
            List<TilskuddPeriode> tilskuddForAvtalePeriode = TilskuddForAvtalePeriode.beregnTilskuddForAvtalePerioden(avtaleInnhold.getSumLonnstilskudd(), nyAvtale.getStartDato(), nyAvtale.getSluttDato());
            avtaleInnhold.getTilskuddPeriode().addAll(tilskuddForAvtalePeriode);
            avtaleInnhold.getTilskuddPeriode().forEach(periode -> periode.setAvtaleInnhold(avtaleInnhold));
        }
    }

    private double getOtpSats(EndreAvtale nyAvtale) {
        double OBLIG_TJENESTEPENSJON_PROSENT_SATS = 0.02;
        if (otpSatsErDefinert(nyAvtale)) {
           return nyAvtale.getOtpSats();
        }
        return  OBLIG_TJENESTEPENSJON_PROSENT_SATS;
    }

    private Boolean otpSatsErDefinert(EndreAvtale nyAvtale) {
        return nyAvtale.getOtpSats() != null;
    }

    private Boolean harAllePåkrevdeFeltForRegneUtTilskuddsPeriode(EndreAvtale nyAvtale){
        return avtaleInnhold.getSumLonnstilskudd() != null && nyAvtale.getStartDato() != null && nyAvtale.getSluttDato() != null;
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

    private Integer getBeregnetOtpBelop(Double optSats, Integer manedslonn, Integer feriepenger) {
        if (erIkkeTomme(optSats, manedslonn, feriepenger)) {
            return (int) Math.round((manedslonn + feriepenger) * optSats);
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
