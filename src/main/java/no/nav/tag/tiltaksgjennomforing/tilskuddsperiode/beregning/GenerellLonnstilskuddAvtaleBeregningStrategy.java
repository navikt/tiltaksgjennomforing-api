package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.convertBigDecimalToInt;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.fikseLøpenumre;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.toBigDecimal;

public abstract class GenerellLonnstilskuddAvtaleBeregningStrategy implements BeregningStrategy {
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

        List<TilskuddPeriode> tilskuddsperioder = beregnTilskuddsperioderForAvtale(
            avtale,
            gjeldendeInnhold.getStartDato(),
            gjeldendeInnhold.getSluttDato()
        );
        if (avtale.getArenaRyddeAvtale() != null) {
            LocalDate standardMigreringsdato = LocalDate.of(2023, 2, 1);
            LocalDate migreringsdato = avtale.getArenaRyddeAvtale()
                .getMigreringsdato() != null ? avtale.getArenaRyddeAvtale()
                .getMigreringsdato() : standardMigreringsdato;

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
        BigDecimal feriepengerBelop = BeregningStrategy.getFeriepengerBelop(
            avtaleInnhold.getFeriepengesats(),
            avtaleInnhold.getManedslonn()
        );
        BigDecimal obligTjenestepensjon = BeregningStrategy.getBeregnetOtpBelop(
            toBigDecimal(avtaleInnhold.getOtpSats()),
            avtaleInnhold.getManedslonn(),
            feriepengerBelop
        );
        BigDecimal arbeidsgiveravgiftBelop = BeregningStrategy.getArbeidsgiverAvgift(
            avtaleInnhold.getManedslonn(), feriepengerBelop, obligTjenestepensjon,
            avtaleInnhold.getArbeidsgiveravgift()
        );
        Integer sumLonnsutgifter = BeregningStrategy.getSumLonnsutgifter(
            avtaleInnhold.getManedslonn(),
            feriepengerBelop,
            obligTjenestepensjon,
            arbeidsgiveravgiftBelop
        );
        Integer sumlønnTilskudd = BeregningStrategy.getSumLonnstilskudd(
            sumLonnsutgifter,
            avtaleInnhold.getLonnstilskuddProsent()
        );
        Integer månedslønnFullStilling = BeregningStrategy.getLønnVedFullStilling(
            avtaleInnhold.getManedslonn(),
            avtaleInnhold.getStillingprosent()
        );
        avtaleInnhold.setFeriepengerBelop(convertBigDecimalToInt(feriepengerBelop));
        avtaleInnhold.setOtpBelop(convertBigDecimalToInt(obligTjenestepensjon));
        avtaleInnhold.setArbeidsgiveravgiftBelop(convertBigDecimalToInt(arbeidsgiveravgiftBelop));
        avtaleInnhold.setSumLonnsutgifter(sumLonnsutgifter);
        avtaleInnhold.setSumLonnstilskudd(sumlønnTilskudd);
        avtaleInnhold.setManedslonn100pst(månedslønnFullStilling);
    }

    @Override
    public List<TilskuddPeriode> beregnTilskuddsperioderForAvtale(
        Avtale avtale,
        LocalDate datoFraOgMed,
        LocalDate datoTilOgMed
    ) {
        List<Periode> reduksjonsperioder = Periode.av(datoFraOgMed, datoTilOgMed).split(getDatoerForReduksjon(avtale));

        return reduksjonsperioder
            .stream()
            .flatMap(p -> p.splitPerMnd()
                .stream()
                .map(datoPar -> lagTilskuddsperiode(avtale, datoPar))
            )
            .toList();

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
            gjeldendeInnhold.getOtpSats()
        );
    }

    @Override
    public boolean nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder(Avtale avtale) {
        AvtaleInnhold gjeldendeInnhold = avtale.getGjeldendeInnhold();
        return Utils.erIkkeTomme(
            gjeldendeInnhold.getStartDato(),
            gjeldendeInnhold.getSluttDato(),
            gjeldendeInnhold.getSumLonnstilskudd()
        );
    }

    @Override
    public Integer getBeløpForPeriode(Avtale avtale, Periode periode) {
        Integer prosentForPeriode = getProsentForPeriode(avtale, periode);
        Integer lonnstilskudd = BeregningStrategy.getSumLonnstilskudd(
            avtale.getGjeldendeInnhold().getSumLonnsutgifter(),
            prosentForPeriode
        );
        return BeregningStrategy.beløpForPeriode(periode, lonnstilskudd);
    }

    public List<Tilskuddstrinn> getTilskuddstrinn(Avtale avtale) {
        LocalDate startdato = avtale.getGjeldendeInnhold().getStartDato();
        LocalDate sluttdato = avtale.getGjeldendeInnhold().getSluttDato();
        Integer sumLonnsutgifter = avtale.getGjeldendeInnhold().getSumLonnsutgifter();

        if (startdato == null || sluttdato == null) {
            return Collections.emptyList();
        }

        return Periode.av(startdato, sluttdato)
            .split(getDatoerForReduksjon(avtale))
            .stream()
            .map(periode -> {
                Integer prosent = getProsentForPeriode(avtale, periode);
                Integer belopPerMnd = BeregningStrategy.getSumLonnstilskudd(sumLonnsutgifter, prosent);
                return new Tilskuddstrinn(
                    periode.getStart(),
                    periode.getSlutt(),
                    prosent,
                    belopPerMnd
                );
            })
            .toList();
    }

    TilskuddPeriode lagTilskuddsperiode(Avtale avtale, Periode periode) {
        Integer beløp = getBeløpForPeriode(avtale, periode);
        Integer prosent = getProsentForPeriode(avtale, periode);
        TilskuddPeriode tilskuddsperiode = new TilskuddPeriode(
            beløp,
            periode.getStart(),
            periode.getSlutt(),
            prosent
        );
        tilskuddsperiode.setAvtale(avtale);
        tilskuddsperiode.setEnhet(avtale.getGjeldendeInnhold().getEnhetKostnadssted());
        tilskuddsperiode.setEnhetsnavn(avtale.getGjeldendeInnhold().getEnhetsnavnKostnadssted());
        return tilskuddsperiode;
    }
}
