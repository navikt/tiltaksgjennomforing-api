package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erIkkeTomme;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.fikseLøpenumre;

public class MidlertidigLonnstilskuddAvtaleBeregningStrategy implements LonnstilskuddAvtaleBeregningStrategy {

    public void genererNyeTilskuddsperioder(Avtale avtale){
        if (avtale.erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_LAGE_NYE_TILSKUDDSPRIODER_INNGAATT_AVTALE);
        }
        List<TilskuddPeriode> tilskuddsperioder = new ArrayList<>();
        avtale.hentTilskuddsperioder().removeIf(t -> (t.getStatus() == TilskuddPeriodeStatus.UBEHANDLET) || (t.getStatus() == TilskuddPeriodeStatus.BEHANDLET_I_ARENA));
        AvtaleInnhold gjeldendeInnhold = avtale.getGjeldendeInnhold();
        Tiltakstype tiltakstype = avtale.getTiltakstype();

        if (erIkkeTomme(gjeldendeInnhold.getStartDato(), gjeldendeInnhold.getSluttDato(), gjeldendeInnhold.getSumLonnstilskudd())) {
            tilskuddsperioder = hentBeregnetTilskuddsperioderForPeriode(avtale,gjeldendeInnhold.getStartDato(), gjeldendeInnhold.getSluttDato());
            if (avtale.getArenaRyddeAvtale() != null) {
                LocalDate standardMigreringsdato = LocalDate.of(2023, 02, 01);
                LocalDate migreringsdato = avtale.getArenaRyddeAvtale().getMigreringsdato() != null ? avtale.getArenaRyddeAvtale().getMigreringsdato() : standardMigreringsdato;

                tilskuddsperioder.forEach(periode -> {
                    // Set status BEHANDLET_I_ARENA på tilskuddsperioder før migreringsdato
                    // Eller skal det være startdato? Er jo den samme datoen som migreringsdato. hmm...
                    if (periode.getSluttDato().minusDays(1).isBefore(migreringsdato)) {
                        periode.setStatus(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);
                    }
                });
            }
        }
        fikseLøpenumre(tilskuddsperioder, 1);
        avtale.leggtilNyeTilskuddsperioder(tilskuddsperioder);
    }

    public void endre(Avtale avtale,EndreTilskuddsberegning endreTilskuddsberegning) {
       AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();
        avtaleInnhold.setArbeidsgiveravgift(endreTilskuddsberegning.getArbeidsgiveravgift());
        avtaleInnhold.setOtpSats(endreTilskuddsberegning.getOtpSats());
        avtaleInnhold.setManedslonn(endreTilskuddsberegning.getManedslonn());
        avtaleInnhold.setFeriepengesats(endreTilskuddsberegning.getFeriepengesats());
        reberegnTotalIAvtale(avtale);
        regnUtDatoOgSumRedusert(avtale);
        avtale.sendTilbakeTilBeslutter();
        avtale.hentTilskuddsperioder().stream().filter(t -> t.getStatus() == TilskuddPeriodeStatus.UBEHANDLET)
                .forEach(t -> t.setBeløp(beregnTilskuddsbeløpForPeriode(avtale,t.getStartDato(), t.getSluttDato())));


    }
    public void reberegnTotalIAvtale(Avtale avtale) {
        AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();
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
        regnUtDatoOgSumRedusert(avtale);
    }
    private void regnUtDatoOgSumRedusert(Avtale avtale) {
        AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();
        LocalDate datoForRedusertProsent = getDatoForRedusertProsent(avtaleInnhold.getStartDato(), avtaleInnhold.getSluttDato(), avtaleInnhold.getLonnstilskuddProsent());
        avtaleInnhold.setDatoForRedusertProsent(datoForRedusertProsent);
        Integer sumLønnstilskuddRedusert = regnUtRedusertLønnstilskudd(avtale);
        avtaleInnhold.setSumLønnstilskuddRedusert(sumLønnstilskuddRedusert);
    }

    /* Default */
    public List<TilskuddPeriode> hentBeregnetTilskuddsperioderForPeriode(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
        AvtaleInnhold gjeldendeInnhold = avtale.getGjeldendeInnhold();

        List<TilskuddPeriode> tilskuddsperioder = beregnTilskuddsperioderForAvtale(
                avtale.getId(),
                avtale.getTiltakstype(),
                gjeldendeInnhold.getSumLonnstilskudd(),
                startDato,
                sluttDato,
                gjeldendeInnhold.getLonnstilskuddProsent(),
                gjeldendeInnhold.getDatoForRedusertProsent(),
                gjeldendeInnhold.getSumLønnstilskuddRedusert());
        tilskuddsperioder.forEach(t -> t.setAvtale(avtale));
        tilskuddsperioder.forEach(t -> t.setEnhet(gjeldendeInnhold.getEnhetKostnadssted()));
        tilskuddsperioder.forEach(t -> t.setEnhetsnavn(gjeldendeInnhold.getEnhetsnavnKostnadssted()));
        return tilskuddsperioder;
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


    public Integer regnUtRedusertLønnstilskudd(Avtale avtale) {
        AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();
        if (avtaleInnhold.getDatoForRedusertProsent() != null && avtaleInnhold.getLonnstilskuddProsent() != null) {
            return getSumLonnsTilskudd(avtaleInnhold.getSumLonnsutgifter(), avtaleInnhold.getLonnstilskuddProsent() - 10);
        } else {
            return null;
        }
    }

    public LocalDate getDatoForRedusertProsent(LocalDate startDato, LocalDate sluttDato, Integer lonnstilskuddprosent) {
        if (startDato == null || sluttDato == null || lonnstilskuddprosent == null) {
            return null;
        }
        if (lonnstilskuddprosent == 40) {
            if (startDato.plusMonths(6).minusDays(1).isBefore(sluttDato)) {
                return startDato.plusMonths(6);
            }

        } else if (lonnstilskuddprosent == 60) {
            if (startDato.plusYears(1).minusDays(1).isBefore(sluttDato)) {
                return startDato.plusYears(1);
            }
        }

        return null;
    }

    public void sjekktilskuddsprosentSats(Integer lonnstilskuddProsent) {
        if (lonnstilskuddProsent != null && (
                lonnstilskuddProsent != 40 && lonnstilskuddProsent != 60)) {
            throw new FeilLonnstilskuddsprosentException();
        }
    }

}
