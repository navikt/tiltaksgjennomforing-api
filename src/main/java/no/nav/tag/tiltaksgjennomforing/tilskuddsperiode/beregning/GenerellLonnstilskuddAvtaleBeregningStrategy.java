package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.convertBigDecimalToInt;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.fikseLøpenumre;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.toBigDecimal;

public abstract class GenerellLonnstilskuddAvtaleBeregningStrategy implements BeregningStrategy {
    public abstract Integer getProsentForPeriode(Avtale avtale, Periode periode);
    public abstract List<LocalDate> getDatoerForReduksjon(Avtale avtale);

    @Override
    public List<TilskuddPeriode> genererNyeTilskuddsperioder(Avtale avtale) {
        if (avtale.erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_LAGE_NYE_TILSKUDDSPRIODER_INNGAATT_AVTALE);
        }

        AvtaleInnhold gjeldendeInnhold = avtale.getGjeldendeInnhold();
        if (!nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder(avtale)) {
            return Collections.emptyList();
        }

        List<TilskuddPeriode> tilskuddsperioder = beregnTilskuddsperioderForAvtale(avtale, gjeldendeInnhold.getStartDato(), gjeldendeInnhold.getSluttDato());
        if (avtale.getArenaRyddeAvtale() != null) {
            LocalDate standardMigreringsdato = LocalDate.of(2023, 2, 1);
            LocalDate migreringsdato = avtale.getArenaRyddeAvtale().getMigreringsdato() != null ? avtale.getArenaRyddeAvtale().getMigreringsdato() : standardMigreringsdato;

            BeregningStrategy.settBehandletIArena(migreringsdato, tilskuddsperioder);
        }
        fikseLøpenumre(tilskuddsperioder, 1);
        return tilskuddsperioder;
    }

    @Override
    public void endreBeregning(Avtale avtale, EndreTilskuddsberegning endreTilskuddsberegning) {
        AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();
        avtaleInnhold.setArbeidsgiveravgift(endreTilskuddsberegning.getArbeidsgiveravgift());
        avtaleInnhold.setOtpSats(endreTilskuddsberegning.getOtpSats());
        avtaleInnhold.setManedslonn(endreTilskuddsberegning.getManedslonn());
        avtaleInnhold.setFeriepengesats(endreTilskuddsberegning.getFeriepengesats());
        reberegnTotal(avtale);
    }

    @Override
    public void reberegnTotal(Avtale avtale) {
        AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();
        BigDecimal feriepengerBelop = BeregningStrategy.getFeriepengerBelop(avtaleInnhold.getFeriepengesats(), avtaleInnhold.getManedslonn());
        BigDecimal obligTjenestepensjon = BeregningStrategy.getBeregnetOtpBelop(toBigDecimal(avtaleInnhold.getOtpSats()), avtaleInnhold.getManedslonn(), feriepengerBelop);
        BigDecimal arbeidsgiveravgiftBelop = BeregningStrategy.getArbeidsgiverAvgift(avtaleInnhold.getManedslonn(), feriepengerBelop, obligTjenestepensjon,
                avtaleInnhold.getArbeidsgiveravgift());
        Integer sumLonnsutgifter = BeregningStrategy.getSumLonnsutgifter(avtaleInnhold.getManedslonn(), feriepengerBelop, obligTjenestepensjon, arbeidsgiveravgiftBelop);
        Integer sumlønnTilskudd = getSumLonnstilskudd(sumLonnsutgifter, avtaleInnhold.getLonnstilskuddProsent());
        Integer månedslønnFullStilling = getLønnVedFullStilling(avtaleInnhold.getManedslonn(), avtaleInnhold.getStillingprosent());
        avtaleInnhold.setFeriepengerBelop(convertBigDecimalToInt(feriepengerBelop));
        avtaleInnhold.setOtpBelop(convertBigDecimalToInt(obligTjenestepensjon));
        avtaleInnhold.setArbeidsgiveravgiftBelop(convertBigDecimalToInt(arbeidsgiveravgiftBelop));
        avtaleInnhold.setSumLonnsutgifter(sumLonnsutgifter);
        avtaleInnhold.setSumLonnstilskudd(sumlønnTilskudd);
        avtaleInnhold.setManedslonn100pst(månedslønnFullStilling);
    }

    @Override
    public List<TilskuddPeriode> beregnTilskuddsperioderForAvtale(Avtale avtale, LocalDate datoFraOgMed, LocalDate datoTilOgMed) {
        List<Periode> reduksjonsperioder = Periode.av(datoFraOgMed, datoTilOgMed).split(getDatoerForReduksjon(avtale));

        return reduksjonsperioder
            .stream()
            .flatMap(p -> p.splitPerMnd()
                .stream()
                .map(datoPar -> {
                    Integer prosentForPeriode = getProsentForPeriode(avtale, datoPar);
                    return lagTilskuddsperiode(
                        avtale,
                        datoPar,
                        getSumLonnstilskudd(avtale.getGjeldendeInnhold().getSumLonnsutgifter(), prosentForPeriode),
                        prosentForPeriode
                    );
                })
            )
            .toList();

    }

    TilskuddPeriode lagTilskuddsperiode(Avtale avtale, Periode periode, int lonnstilskudd, int lonnstilskuddProsent) {
        Integer beløp = BeregningStrategy.beløpForPeriode(periode.getStart(), periode.getSlutt(), lonnstilskudd);
        TilskuddPeriode tilskuddsperiode = new TilskuddPeriode(beløp, periode.getStart(), periode.getSlutt(), lonnstilskuddProsent);
        tilskuddsperiode.setAvtale(avtale);
        tilskuddsperiode.setEnhet(avtale.getGjeldendeInnhold().getEnhetKostnadssted());
        tilskuddsperiode.setEnhetsnavn(avtale.getGjeldendeInnhold().getEnhetsnavnKostnadssted());
        return tilskuddsperiode;
    }

    @Override
    public boolean nødvendigeFelterErUtfyltForBeregningAvTilskuddsbeløp(Avtale avtale) {
        var gjeldendeInnhold = avtale.getGjeldendeInnhold();
        return Utils.erIkkeTomme(
                gjeldendeInnhold.getStartDato(),
                gjeldendeInnhold.getSluttDato(),
                gjeldendeInnhold.getSumLonnstilskudd(),
                gjeldendeInnhold.getLonnstilskuddProsent(),
                gjeldendeInnhold.getArbeidsgiveravgift(),
                gjeldendeInnhold.getManedslonn(),
                gjeldendeInnhold.getOtpSats());
    }

    @Override
    public boolean nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder(Avtale avtale) {
        AvtaleInnhold gjeldendeInnhold = avtale.getGjeldendeInnhold();
        return Utils.erIkkeTomme(gjeldendeInnhold.getStartDato(), gjeldendeInnhold.getSluttDato(), gjeldendeInnhold.getSumLonnstilskudd());
    }

    Integer getLønnVedFullStilling(Integer manedslonn, BigDecimal stillingsProsent) {
        if (manedslonn == null || stillingsProsent == null || stillingsProsent.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return BigDecimal.valueOf(manedslonn).multiply(new BigDecimal(100)).divide(stillingsProsent, RoundingMode.HALF_UP).intValue();
    }

    Integer getSumLonnstilskudd(Integer sumLonnsutgifter, Integer lonnstilskuddProsent) {
        if (sumLonnsutgifter == null || lonnstilskuddProsent == null) {
            return null;
        }
        double lonnstilskuddProsentSomDecimal = lonnstilskuddProsent.doubleValue() / 100;
        return (int) Math.round(sumLonnsutgifter * lonnstilskuddProsentSomDecimal);
    }
}
