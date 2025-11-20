package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;


import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.MentorTilskuddsperioderToggle;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.convertBigDecimalToInt;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.fikseLøpenumre;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.toBigDecimal;

public class MentorBeregningStrategy implements BeregningStrategy {

    @Override
    public void reberegnTotal(Avtale avtale) {
        AvtaleInnhold innhold = avtale.getGjeldendeInnhold();
        if (!nødvendigeFelterErUtfyltForBeregningAvTilskuddsbeløp(avtale)) {
            return;
        }
        var mentorsMånedslønn = Double.valueOf(innhold.getMentorTimelonn() * innhold.getMentorAntallTimer()).intValue();

        BigDecimal feriepengerBelop = BeregningStrategy.getFeriepengerBelop(innhold.getFeriepengesats(), mentorsMånedslønn);
        BigDecimal obligTjenestepensjon = BeregningStrategy.getBeregnetOtpBelop(toBigDecimal(innhold.getOtpSats()), mentorsMånedslønn, feriepengerBelop);
        BigDecimal arbeidsgiveravgiftBelop = BeregningStrategy.getArbeidsgiverAvgift(mentorsMånedslønn, feriepengerBelop, obligTjenestepensjon, innhold.getArbeidsgiveravgift());
        Integer sumLonnsutgifter = BeregningStrategy.getSumLonnsutgifter(mentorsMånedslønn, feriepengerBelop, obligTjenestepensjon, arbeidsgiveravgiftBelop);
        innhold.setFeriepengerBelop(convertBigDecimalToInt(feriepengerBelop));
        innhold.setOtpBelop(convertBigDecimalToInt(obligTjenestepensjon));
        innhold.setArbeidsgiveravgiftBelop(convertBigDecimalToInt(arbeidsgiveravgiftBelop));
        innhold.setSumLonnsutgifter(sumLonnsutgifter);
    }

    @Override
    public boolean nødvendigeFelterErUtfyltForBeregningAvTilskuddsbeløp(Avtale avtale) {
        AvtaleInnhold innhold = avtale.getGjeldendeInnhold();
        return Utils.erIkkeTomme(
            innhold.getMentorAntallTimer(),
            innhold.getMentorTimelonn(),
            innhold.getFeriepengesats(),
            innhold.getArbeidsgiveravgift(),
            innhold.getOtpSats()
        );
    }

    @Override
    public boolean nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder(Avtale avtale) {
        AvtaleInnhold gjeldendeInnhold = avtale.getGjeldendeInnhold();
        return Utils.erIkkeTomme(gjeldendeInnhold.getStartDato(), gjeldendeInnhold.getSluttDato(), gjeldendeInnhold.getSumLonnsutgifter());
    }

    @Override
    public List<TilskuddPeriode> hentTilskuddsperioderForPeriode(
        Avtale avtale,
        LocalDate startDato,
        LocalDate sluttDato
    ) {
        return List.of(); // TODO: Fiks denne!!!!! FØR MIGRERING!!
    }

    private List<TilskuddPeriode> beregnTilskuddsperioderForAvtale(Integer tilskuddsbeløpPerMåned, LocalDate datoFraOgMed, LocalDate datoTilOgMed) {
            return BeregningStrategy.lagPeriode(datoFraOgMed, datoTilOgMed).stream().map(datoPar -> {
                Integer beløp = BeregningStrategy.beløpForPeriode(
                    datoPar.getStart(),
                    datoPar.getSlutt(),
                    tilskuddsbeløpPerMåned
                );
                return new TilskuddPeriode(beløp, datoPar.getStart(), datoPar.getSlutt(), 100);
            }).collect(Collectors.toList());
    }


        @Override
    public List<TilskuddPeriode> genererNyeTilskuddsperioder(Avtale avtale) {
        if (!MentorTilskuddsperioderToggle.isEnabled()) {
            return Collections.emptyList();
        }

        if (avtale.erAvtaleInngått()) {
            return Collections.emptyList();
        }
        if (!nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder(avtale)) {
            return Collections.emptyList();
        }
        AvtaleInnhold innhold = avtale.getGjeldendeInnhold();

        LocalDate start = innhold.getStartDato();
        LocalDate slutt = innhold.getSluttDato();

        List<TilskuddPeriode> perioder = beregnTilskuddsperioderForAvtale(innhold.getSumLonnsutgifter(), start, slutt);

        perioder.forEach(p -> p.setAvtale(avtale));
        perioder.forEach(p -> p.setEnhet(innhold.getEnhetKostnadssted()));
        perioder.forEach(p -> p.setEnhetsnavn(innhold.getEnhetsnavnKostnadssted()));
        fikseLøpenumre(perioder, 1);
        return perioder;
    }

    /** Metode som kalles ifbm endring av tilskuddsbergning-felter etter at en avtale er inngått. */
    @Override
    public void endreBeregning(Avtale avtale, EndreTilskuddsberegning endreTilskuddsberegning) {
        AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();
        avtaleInnhold.setArbeidsgiveravgift(endreTilskuddsberegning.getArbeidsgiveravgift());
        avtaleInnhold.setOtpSats(endreTilskuddsberegning.getOtpSats());
        avtaleInnhold.setFeriepengesats(endreTilskuddsberegning.getFeriepengesats());
        avtaleInnhold.setMentorAntallTimer(endreTilskuddsberegning.getMentorAntallTimer());
        avtaleInnhold.setMentorTimelonn(endreTilskuddsberegning.getMentorTimelonn());
        reberegnTotal(avtale);
    }

    @Override
    public Integer beregnTilskuddsbeløpForPeriode(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
        if (!nødvendigeFelterErUtfyltForBeregningAvTilskuddsbeløp(avtale)) {
            return null;
        }
        AvtaleInnhold innhold = avtale.getGjeldendeInnhold();

        int månedsLonn = beregnMånedligTilskudd(innhold.getMentorAntallTimer(), innhold.getMentorTimelonn());
        return BeregningStrategy.beløpForPeriode(startDato, sluttDato, månedsLonn);
    }

    private Integer beregnMånedligTilskudd(double antalTimerIMåned, double timelonn) {
        BigDecimal mentorsMånedslønn = BigDecimal.valueOf(antalTimerIMåned).multiply(BigDecimal.valueOf(timelonn));
        return mentorsMånedslønn.setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
