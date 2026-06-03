package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeStatus;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static no.nav.tag.tiltaksgjennomforing.utils.DatoUtils.sisteDatoIMnd;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.convertBigDecimalToInt;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erIkkeTomme;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.fikseLøpenumre;

public abstract class BeregningStrategy {
    private static final BigDecimal DAGER_I_MÅNED = new BigDecimal("30.4375");

    protected Avtale avtale;

    public static BeregningStrategy create(Avtale avtale){
        return switch (avtale.getTiltakstype()) {
            case ARBEIDSTRENING, INKLUDERINGSTILSKUDD -> new IkkeLonnstilskuddAvtaleBeregningStrategy(avtale);
            case MENTOR -> new MentorBeregningStrategy(avtale);
            case MIDLERTIDIG_LONNSTILSKUDD -> new MidlertidigLonnstilskuddAvtaleBeregningStrategy(avtale);
            case VARIG_LONNSTILSKUDD -> new VarigLonnstilskuddAvtaleBeregningStrategy(avtale);
            case SOMMERJOBB -> new SommerjobbLonnstilskuddAvtaleBeregningStrategy(avtale);
            case VTAO -> new VTAOAvtaleBeregningStrategy(avtale);
            case FIREARIG_LONNSTILSKUDD -> new FirearigLonnstilskuddBeregningStrategy(avtale);
        };
    }

    public abstract List<TilskuddPeriode> genererNyeTilskuddsperioder();
    public abstract void endreBeregning(EndreTilskuddsberegning endreTilskuddsberegning);
    public abstract void reberegnTotal();
    public abstract boolean nødvendigeFelterErUtfyltForBeregningAvTilskuddsbeløp();
    public abstract boolean nødvendigeFelterErUtfyltForÅGenerereTilskuddsperioder();
    public abstract List<TilskuddPeriode> beregnTilskuddsperioderForAvtale(LocalDate startDato, LocalDate sluttDato);

    public BeregningStrategy(Avtale avtale) {
        this.avtale = avtale;
    }

    public void forleng(LocalDate gammelSluttDato, LocalDate nySluttDato){
        Set<TilskuddPeriode> tilskuddPeriode = avtale.getTilskuddPeriode();

        if (tilskuddPeriode.isEmpty()) {
            return;
        }
        Optional<TilskuddPeriode> periodeMedHøyestLøpenummer = tilskuddPeriode.stream().max(Comparator.comparing(TilskuddPeriode::getLøpenummer));
        TilskuddPeriode sisteTilskuddsperiode = periodeMedHøyestLøpenummer.get();
        if (sisteTilskuddsperiode.getStatus() == TilskuddPeriodeStatus.UBEHANDLET) {
            // Kan utvide siste tilskuddsperiode hvis den er ubehandlet
            tilskuddPeriode.remove(sisteTilskuddsperiode);
            List<TilskuddPeriode> nyeTilskuddperioder = beregnTilskuddsperioderForAvtale(sisteTilskuddsperiode.getStartDato(), nySluttDato);
            fikseLøpenumre(nyeTilskuddperioder, sisteTilskuddsperiode.getLøpenummer());
            tilskuddPeriode.addAll(nyeTilskuddperioder);
        } else if (sisteTilskuddsperiode.getSluttDato().isBefore(sisteDatoIMnd(sisteTilskuddsperiode.getSluttDato())) && sisteTilskuddsperiode.getStatus() == TilskuddPeriodeStatus.GODKJENT && (!sisteTilskuddsperiode.erRefusjonGodkjent() && !sisteTilskuddsperiode.erUtbetalt())) {
            avtale.annullerTilskuddsperiode(sisteTilskuddsperiode);
            List<TilskuddPeriode> nyeTilskuddperioder = beregnTilskuddsperioderForAvtale(sisteTilskuddsperiode.getStartDato(), nySluttDato);
            fikseLøpenumre(nyeTilskuddperioder, sisteTilskuddsperiode.getLøpenummer() + 1);
            tilskuddPeriode.addAll(nyeTilskuddperioder);
        } else {
            // Regner ut nye perioder fra gammel avtaleslutt til ny avtaleslutt
            List<TilskuddPeriode> nyeTilskuddperioder = beregnTilskuddsperioderForAvtale(gammelSluttDato.plusDays(1), nySluttDato);
            fikseLøpenumre(nyeTilskuddperioder, sisteTilskuddsperiode.getLøpenummer() + 1);
            tilskuddPeriode.addAll(nyeTilskuddperioder);
        }
    }

    public Integer getBeløpForPeriode(AvtaleInnhold avtaleInnhold, Periode periode) {
        return 0;
    }

    public Integer getProsentForPeriode(AvtaleInnhold avtaleInnhold, Periode periode) {
        return null;
    }

    protected static BigDecimal getFeriepengerBelop(BigDecimal feriepengersats, Integer manedslonn) {
        if (erIkkeTomme(feriepengersats, manedslonn)) {
            return (feriepengersats.multiply(BigDecimal.valueOf(manedslonn)));
        }
        return null;
    }

    protected static BigDecimal getFeriepengerBelop(BigDecimal feriepengersats, Double manedslonn) {
        if (erIkkeTomme(feriepengersats, manedslonn)) {
            return (feriepengersats.multiply(BigDecimal.valueOf(manedslonn)));
        }
        return null;
    }

    protected static BigDecimal getBeregnetOtpBelop(BigDecimal optSats, Integer manedslonn, BigDecimal feriepenger) {
        if (erIkkeTomme(optSats, manedslonn, feriepenger)) {
            return (optSats.multiply(BigDecimal.valueOf(manedslonn).add(feriepenger)));
        }
        return null;
    }

    protected static BigDecimal getBeregnetOtpBelop(BigDecimal optSats, Double manedslonn, BigDecimal feriepenger) {
        if (erIkkeTomme(optSats, manedslonn, feriepenger)) {
            return (optSats.multiply(BigDecimal.valueOf(manedslonn).add(feriepenger)));
        }
        return null;
    }

    protected static BigDecimal getArbeidsgiverAvgift(
        Integer manedslonn,
        BigDecimal feriepengerBelop,
        BigDecimal obligTjenestepensjon,
        BigDecimal arbeidsgiveravgift
    ) {
        if (erIkkeTomme(manedslonn, feriepengerBelop, obligTjenestepensjon, arbeidsgiveravgift)) {
            return arbeidsgiveravgift.multiply(BigDecimal.valueOf(manedslonn).add(feriepengerBelop).add(obligTjenestepensjon));
        }
        return null;
    }

    protected static BigDecimal getArbeidsgiverAvgift(
        Double manedslonn,
        BigDecimal feriepengerBelop,
        BigDecimal obligTjenestepensjon,
        BigDecimal arbeidsgiveravgift
    ) {
        if (erIkkeTomme(manedslonn, feriepengerBelop, obligTjenestepensjon, arbeidsgiveravgift)) {
            return arbeidsgiveravgift.multiply(BigDecimal.valueOf(manedslonn).add(feriepengerBelop).add(obligTjenestepensjon));
        }
        return null;
    }

    protected static Integer getSumLonnsutgifter(
        Integer manedslonn,
        BigDecimal feriepengerBelop,
        BigDecimal obligTjenestepensjon,
        BigDecimal arbeidsgiveravgiftBelop
    ) {
        if (erIkkeTomme(feriepengerBelop, obligTjenestepensjon, arbeidsgiveravgiftBelop)) {
            return manedslonn + convertBigDecimalToInt(feriepengerBelop.add(obligTjenestepensjon).add(arbeidsgiveravgiftBelop));
        }
        return null;
    }

    protected static Integer getSumLonnsutgifter(
        Double manedslonn,
        BigDecimal feriepengerBelop,
        BigDecimal obligTjenestepensjon,
        BigDecimal arbeidsgiveravgiftBelop
    ) {
        if (erIkkeTomme(feriepengerBelop, obligTjenestepensjon, arbeidsgiveravgiftBelop)) {
            return manedslonn.intValue() + convertBigDecimalToInt(feriepengerBelop.add(obligTjenestepensjon).add(arbeidsgiveravgiftBelop));
        }
        return null;
    }

    protected static Integer getLønnVedFullStilling(Integer manedslonn, BigDecimal stillingsProsent) {
        if (manedslonn == null || stillingsProsent == null || stillingsProsent.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return BigDecimal.valueOf(manedslonn).multiply(new BigDecimal(100)).divide(stillingsProsent, RoundingMode.HALF_UP).intValue();
    }

    protected static Integer getSumLonnstilskudd(Integer sumLonnsutgifter, Integer lonnstilskuddProsent) {
        if (sumLonnsutgifter == null || lonnstilskuddProsent == null) {
            return null;
        }
        double lonnstilskuddProsentSomDecimal = lonnstilskuddProsent.doubleValue() / 100;
        return (int) Math.round(sumLonnsutgifter * lonnstilskuddProsentSomDecimal);
    }

    public static Integer beløpForPeriode(Periode periode, Integer tilskuddPerMåned) {
        Period period = Period.between(periode.getStart(), periode.getSlutt().plusDays(1));
        Integer sumHeleMåneder = period.getMonths() * tilskuddPerMåned;
        BigDecimal dagsats = new BigDecimal(tilskuddPerMåned).divide(DAGER_I_MÅNED, 10, RoundingMode.HALF_UP);
        Integer sumEnkeltdager = dagsats.multiply(BigDecimal.valueOf(period.getDays()), MathContext.UNLIMITED).setScale(0, RoundingMode.HALF_UP).intValue();
        return sumHeleMåneder + sumEnkeltdager;
    }

    public static void settBehandletIArena(LocalDate migreringsdato, List<TilskuddPeriode> tilskuddPeriode) {
        // Her har vi en antakelse om at tilskuddsperioder ikke kan gå på tvers av måneder, og at vi derfor
        // kan vilkårlig velge sluttdatoen som utgangspunktet for beregning av hvorvidt tilskuddsperioden
        // er behandlet i arena eller ikke.
        tilskuddPeriode.forEach(periode -> {
            if (periode.getSluttDato().isBefore(migreringsdato)) {
                periode.setStatus(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);
            }
        });
    }
}
