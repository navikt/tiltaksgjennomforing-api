package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeStatus;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.convertBigDecimalToInt;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erIkkeTomme;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.fikseLøpenumre;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.toBigDecimal;

public class GenerellLonnstilskuddAvtaleBeregningStrategy implements BeregningStrategy {

    public List<TilskuddPeriode> genererNyeTilskuddsperioder(Avtale avtale) {
        if (avtale.erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_LAGE_NYE_TILSKUDDSPRIODER_INNGAATT_AVTALE);
        }

        AvtaleInnhold gjeldendeInnhold = avtale.getGjeldendeInnhold();
        if (!nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder(avtale)) {
            return Collections.emptyList();
        }

        List<TilskuddPeriode> tilskuddsperioder = hentTilskuddsperioderForPeriode(avtale, gjeldendeInnhold.getStartDato(), gjeldendeInnhold.getSluttDato());
        if (avtale.getArenaRyddeAvtale() != null) {
            LocalDate standardMigreringsdato = LocalDate.of(2023, 2, 1);
            LocalDate migreringsdato = avtale.getArenaRyddeAvtale().getMigreringsdato() != null ? avtale.getArenaRyddeAvtale().getMigreringsdato() : standardMigreringsdato;

            tilskuddsperioder.forEach(periode -> {
                // Set status BEHANDLET_I_ARENA på tilskuddsperioder før migreringsdato
                // Eller skal det være startdato? Er jo den samme datoen som migreringsdato. hmm...
                if (periode.getSluttDato().minusDays(1).isBefore(migreringsdato)) {
                    periode.setStatus(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);
                }
            });
        }
        fikseLøpenumre(tilskuddsperioder, 1);
        return tilskuddsperioder;
    }

    public void endreBeregning(Avtale avtale, EndreTilskuddsberegning endreTilskuddsberegning) {
        AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();
        avtaleInnhold.setArbeidsgiveravgift(endreTilskuddsberegning.getArbeidsgiveravgift());
        avtaleInnhold.setOtpSats(endreTilskuddsberegning.getOtpSats());
        avtaleInnhold.setManedslonn(endreTilskuddsberegning.getManedslonn());
        avtaleInnhold.setFeriepengesats(endreTilskuddsberegning.getFeriepengesats());
        reberegnTotal(avtale);
    }

    public void reberegnTotal(Avtale avtale) {
        AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();
        BigDecimal feriepengerBelop = BeregningStrategy.getFeriepengerBelop(avtaleInnhold.getFeriepengesats(), avtaleInnhold.getManedslonn());
        BigDecimal obligTjenestepensjon = BeregningStrategy.getBeregnetOtpBelop(toBigDecimal(avtaleInnhold.getOtpSats()), avtaleInnhold.getManedslonn(), feriepengerBelop);
        BigDecimal arbeidsgiveravgiftBelop = BeregningStrategy.getArbeidsgiverAvgift(avtaleInnhold.getManedslonn(), feriepengerBelop, obligTjenestepensjon,
                avtaleInnhold.getArbeidsgiveravgift());
        Integer sumLonnsutgifter = BeregningStrategy.getSumLonnsutgifter(avtaleInnhold.getManedslonn(), feriepengerBelop, obligTjenestepensjon, arbeidsgiveravgiftBelop);
        Integer sumlønnTilskudd = getSumLonnsTilskudd(sumLonnsutgifter, avtaleInnhold.getLonnstilskuddProsent());
        Integer månedslønnFullStilling = getLønnVedFullStilling(sumLonnsutgifter, avtaleInnhold.getStillingprosent());
        avtaleInnhold.setFeriepengerBelop(convertBigDecimalToInt(feriepengerBelop));
        avtaleInnhold.setOtpBelop(convertBigDecimalToInt(obligTjenestepensjon));
        avtaleInnhold.setArbeidsgiveravgiftBelop(convertBigDecimalToInt(arbeidsgiveravgiftBelop));
        avtaleInnhold.setSumLonnsutgifter(sumLonnsutgifter);
        avtaleInnhold.setSumLonnstilskudd(sumlønnTilskudd);
        avtaleInnhold.setManedslonn100pst(månedslønnFullStilling);
    }

    public List<TilskuddPeriode> beregnTilskuddsperioderForAvtale(UUID id, Tiltakstype tiltakstype, Integer sumLønnstilskuddPerMåned, LocalDate datoFraOgMed, LocalDate datoTilOgMed, Integer lonnstilskuddprosent, LocalDate datoForRedusertProsent, Integer sumLønnstilskuddPerMånedRedusert) {
        if (datoForRedusertProsent == null) {
            // Ingen reduserte peridoder   -----60-----60------60------ |
            return BeregningStrategy.lagPeriode(datoFraOgMed, datoTilOgMed).stream().map(datoPar -> {
                Integer beløp = BeregningStrategy.beløpForPeriode(datoPar.getStart(), datoPar.getSlutt(), sumLønnstilskuddPerMåned);
                return new TilskuddPeriode(beløp, datoPar.getStart(), datoPar.getSlutt(), lonnstilskuddprosent);
            }).collect(Collectors.toList());
        } else {
            if (datoFraOgMed.isBefore(datoForRedusertProsent.plusDays(1)) && datoTilOgMed.isAfter(datoForRedusertProsent.minusDays(1))) {
                // Både ikke reduserte og reduserte   ---60---60-----50----|--50----50-----
                List<TilskuddPeriode> tilskuddperioderFørRedusering = BeregningStrategy.lagPeriode(datoFraOgMed, datoForRedusertProsent.minusDays(1)).stream().map(datoPar -> {
                    Integer beløp = BeregningStrategy.beløpForPeriode(datoPar.getStart(), datoPar.getSlutt(), sumLønnstilskuddPerMåned);
                    return new TilskuddPeriode(beløp, datoPar.getStart(), datoPar.getSlutt(), lonnstilskuddprosent);
                }).toList();

                List<TilskuddPeriode> tilskuddperioderEtterRedusering = BeregningStrategy.lagPeriode(datoForRedusertProsent, datoTilOgMed).stream().map(datoPar -> {
                    Integer beløp = BeregningStrategy.beløpForPeriode(datoPar.getStart(), datoPar.getSlutt(), sumLønnstilskuddPerMånedRedusert);
                    return new TilskuddPeriode(beløp, datoPar.getStart(), datoPar.getSlutt(), BeregningStrategy.getRedusertLonnstilskuddprosent(tiltakstype, lonnstilskuddprosent));
                }).toList();

                ArrayList<TilskuddPeriode> tilskuddsperioder = new ArrayList<>();
                tilskuddsperioder.addAll(tilskuddperioderFørRedusering);
                tilskuddsperioder.addAll(tilskuddperioderEtterRedusering);
                return tilskuddsperioder;
            } else if (datoFraOgMed.isAfter(datoForRedusertProsent)) {
                // Kun redusete peridoer      ---60----60----60---50---|--50----50---50--50--
                List<TilskuddPeriode> tilskuddperioderEtterRedusering = BeregningStrategy.lagPeriode(datoFraOgMed, datoTilOgMed).stream().map(datoPar -> {
                    Integer beløp = BeregningStrategy.beløpForPeriode(datoPar.getStart(), datoPar.getSlutt(), sumLønnstilskuddPerMånedRedusert);
                    return new TilskuddPeriode(beløp, datoPar.getStart(), datoPar.getSlutt(), BeregningStrategy.getRedusertLonnstilskuddprosent(tiltakstype, lonnstilskuddprosent));
                }).toList();
                return new ArrayList<>(tilskuddperioderEtterRedusering);
            } else {
                throw new FeilkodeException(Feilkode.FORLENG_MIDLERTIDIG_IKKE_TILGJENGELIG);
            }
        }

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


    public List<TilskuddPeriode> hentTilskuddsperioderForPeriode(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
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

    Integer getLønnVedFullStilling(Integer sumUtgifter, BigDecimal stillingsProsent) {
        if (sumUtgifter == null || stillingsProsent == null || stillingsProsent.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return BigDecimal.valueOf(sumUtgifter).multiply(new BigDecimal(100)).divide(stillingsProsent, RoundingMode.HALF_UP).intValue();
    }

    Integer getSumLonnsTilskudd(Integer sumLonnsutgifter, Integer lonnstilskuddProsent) {
        if (sumLonnsutgifter == null || lonnstilskuddProsent == null) {
            return null;
        }
        double lonnstilskuddProsentSomDecimal = lonnstilskuddProsent.doubleValue() / 100;
        return (int) Math.round(sumLonnsutgifter * lonnstilskuddProsentSomDecimal);
    }


    public LocalDate getDatoForRedusertProsent(LocalDate startDato, LocalDate sluttDato, Integer lonnstilskuddprosent) {
        if (startDato == null || sluttDato == null || lonnstilskuddprosent == null) {
            return null;
        }
        if (startDato.plusYears(1).minusDays(1).isBefore(sluttDato)) {
            return startDato.plusYears(1);
        }
        return null;
    }
}
