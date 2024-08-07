package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

import static no.nav.tag.tiltaksgjennomforing.utils.DatoUtils.sisteDatoIMnd;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.fikseLøpenumre;

public interface LonnstilskuddAvtaleBeregningStrategy {

    BigDecimal DAGER_I_MÅNED = new BigDecimal("30.4375");
    int ANTALL_MÅNEDER_I_EN_PERIODE = 1;

    void genererNyeTilskuddsperioder(Avtale avtale);
    List<TilskuddPeriode> hentBeregnetTilskuddsperioderForPeriode(Avtale avtale, LocalDate startDato, LocalDate sluttDato);
    void endre(Avtale avtale,EndreTilskuddsberegning endreTilskuddsberegning);
    void reberegnTotalIAvtale(Avtale avtale);
    default void forleng(Avtale avtale, LocalDate gammelSluttDato, LocalDate nySluttDato){
        Set<TilskuddPeriode> tilskuddPeriode = avtale.hentTilskuddsperioder();

        if (tilskuddPeriode.isEmpty()) {
            return;
        }
        Optional<TilskuddPeriode> periodeMedHøyestLøpenummer = tilskuddPeriode.stream().max(Comparator.comparing(TilskuddPeriode::getLøpenummer));
        TilskuddPeriode sisteTilskuddsperiode = periodeMedHøyestLøpenummer.get();
        if (sisteTilskuddsperiode.getStatus() == TilskuddPeriodeStatus.UBEHANDLET) {
            // Kan utvide siste tilskuddsperiode hvis den er ubehandlet
            tilskuddPeriode.remove(sisteTilskuddsperiode);
            List<TilskuddPeriode> nyeTilskuddperioder = hentBeregnetTilskuddsperioderForPeriode(avtale,sisteTilskuddsperiode.getStartDato(), nySluttDato);
            fikseLøpenumre(nyeTilskuddperioder, sisteTilskuddsperiode.getLøpenummer());
            tilskuddPeriode.addAll(nyeTilskuddperioder);
        } else if (sisteTilskuddsperiode.getSluttDato().isBefore(sisteDatoIMnd(sisteTilskuddsperiode.getSluttDato())) && sisteTilskuddsperiode.getStatus() == TilskuddPeriodeStatus.GODKJENT && (!sisteTilskuddsperiode.erRefusjonGodkjent() && !sisteTilskuddsperiode.erUtbetalt())) {
            avtale.annullerTilskuddsperiode(sisteTilskuddsperiode);
            List<TilskuddPeriode> nyeTilskuddperioder = hentBeregnetTilskuddsperioderForPeriode(avtale,sisteTilskuddsperiode.getStartDato(), nySluttDato);
            fikseLøpenumre(nyeTilskuddperioder, sisteTilskuddsperiode.getLøpenummer() + 1);
            tilskuddPeriode.addAll(nyeTilskuddperioder);
        } else {
            // Regner ut nye perioder fra gammel avtaleslutt til ny avtaleslutt
            List<TilskuddPeriode> nyeTilskuddperioder = hentBeregnetTilskuddsperioderForPeriode(avtale,gammelSluttDato.plusDays(1), nySluttDato);
            fikseLøpenumre(nyeTilskuddperioder, sisteTilskuddsperiode.getLøpenummer() + 1);
            tilskuddPeriode.addAll(nyeTilskuddperioder);
        }
    }

    default Integer beregnTilskuddsbeløpForPeriode(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
        return beløpForPeriode(startDato,
                sluttDato,
                avtale.getGjeldendeInnhold().getDatoForRedusertProsent(),
                avtale.getGjeldendeInnhold().getSumLonnstilskudd(),
                avtale.getGjeldendeInnhold().getSumLønnstilskuddRedusert());
    }

    default  List<TilskuddPeriode> beregnTilskuddsperioderForAvtale(UUID id, Tiltakstype tiltakstype, Integer sumLønnstilskuddPerMåned, LocalDate datoFraOgMed, LocalDate datoTilOgMed, Integer lonnstilskuddprosent, LocalDate datoForRedusertProsent, Integer sumLønnstilskuddPerMånedRedusert) {
        if (datoForRedusertProsent == null) {
            // Ingen reduserte peridoder   -----60-----60------60------ |
            return lagPeriode(datoFraOgMed, datoTilOgMed).stream().map(datoPar -> {
                Integer beløp = beløpForPeriode(datoPar.getStart(), datoPar.getSlutt(), sumLønnstilskuddPerMåned);
                return new TilskuddPeriode(beløp, datoPar.getStart(), datoPar.getSlutt(), lonnstilskuddprosent);
            }).collect(Collectors.toList());
        } else {
            if (datoFraOgMed.isBefore(datoForRedusertProsent.plusDays(1)) && datoTilOgMed.isAfter(datoForRedusertProsent.minusDays(1))) {
                // Både ikke reduserte og reduserte   ---60---60-----50----|--50----50-----
                List<TilskuddPeriode> tilskuddperioderFørRedusering = lagPeriode(datoFraOgMed, datoForRedusertProsent.minusDays(1)).stream().map(datoPar -> {
                    Integer beløp = beløpForPeriode(datoPar.getStart(), datoPar.getSlutt(), sumLønnstilskuddPerMåned);// TODO: IKKE TESTET
                    return new TilskuddPeriode(beløp, datoPar.getStart(), datoPar.getSlutt(), lonnstilskuddprosent);
                }).toList();

                List<TilskuddPeriode> tilskuddperioderEtterRedusering = lagPeriode(datoForRedusertProsent, datoTilOgMed).stream().map(datoPar -> {
                    Integer beløp = beløpForPeriode(datoPar.getStart(), datoPar.getSlutt(), sumLønnstilskuddPerMånedRedusert);// TODO: IKKE TESTET
                    return new TilskuddPeriode(beløp, datoPar.getStart(), datoPar.getSlutt(), getLonnstilskuddProsent(tiltakstype, lonnstilskuddprosent));
                }).toList();

                ArrayList<TilskuddPeriode> tilskuddsperioder = new ArrayList<>();
                tilskuddsperioder.addAll(tilskuddperioderFørRedusering);
                tilskuddsperioder.addAll(tilskuddperioderEtterRedusering);
                return tilskuddsperioder;
            } else if (datoFraOgMed.isAfter(datoForRedusertProsent)) {
                // Kun redusete peridoer      ---60----60----60---50---|--50----50---50--50--
                List<TilskuddPeriode> tilskuddperioderEtterRedusering = lagPeriode(datoFraOgMed, datoTilOgMed).stream().map(datoPar -> {
                    Integer beløp = beløpForPeriode(datoPar.getStart(), datoPar.getSlutt(), sumLønnstilskuddPerMånedRedusert);
                    return new TilskuddPeriode(beløp, datoPar.getStart(), datoPar.getSlutt(), getLonnstilskuddProsent(tiltakstype, lonnstilskuddprosent));
                }).toList();
                return new ArrayList<>(tilskuddperioderEtterRedusering);
            } else {
                //log.error("Uventet feil i utregning av tilskuddsperioder med startdato: {}, sluttdato: {}, datoForRedusertProsent: {}, avtaleId: {}", datoFraOgMed, datoTilOgMed, datoForRedusertProsent, id);
                throw new FeilkodeException(Feilkode.FORLENG_MIDLERTIDIG_IKKE_TILGJENGELIG);
            }
        }

    }

    private static int getLonnstilskuddProsent(Tiltakstype tiltakstype, Integer lonnstilskuddprosent) {
        if(tiltakstype == Tiltakstype.VARIG_LONNSTILSKUDD){
            if(lonnstilskuddprosent >= 68) return 67;
            return lonnstilskuddprosent;
        }
        return lonnstilskuddprosent - 10;
    }

    private static Integer beløpForPeriode(LocalDate datoFraOgMed, LocalDate datoTilOgMed, LocalDate datoForRedusertProsent, Integer sumLønnstilskuddPerMåned, Integer sumLønnstilskuddPerMånedRedusert) {
        if (datoForRedusertProsent == null || datoTilOgMed.isBefore(datoForRedusertProsent)) {
            return beløpForPeriode(datoFraOgMed, datoTilOgMed, sumLønnstilskuddPerMåned);
        } else {
            return beløpForPeriode(datoFraOgMed, datoTilOgMed, sumLønnstilskuddPerMånedRedusert);
        }
    }

    static Integer beløpForPeriode(LocalDate fra, LocalDate til, Integer sumLønnstilskuddPerMåned) {
        Period period = Period.between(fra, til.plusDays(1));
        Integer sumHeleMåneder = period.getMonths() * sumLønnstilskuddPerMåned;
        BigDecimal dagsats = new BigDecimal(sumLønnstilskuddPerMåned).divide(DAGER_I_MÅNED, 10, RoundingMode.HALF_UP);
        Integer sumEnkeltdager = dagsats.multiply(BigDecimal.valueOf(period.getDays()), MathContext.UNLIMITED).setScale(0, RoundingMode.HALF_UP).intValue();
        return sumHeleMåneder + sumEnkeltdager;
    }

    static List<Periode> lagPeriode(LocalDate datoFraOgMed, LocalDate datoTilOgMed) {
        if (datoFraOgMed.isAfter(datoTilOgMed)) {
            return List.of();
        }
        List<LocalDate> startDatoer = datoFraOgMed.datesUntil(datoTilOgMed.plusDays(1), Period.ofMonths(ANTALL_MÅNEDER_I_EN_PERIODE)).toList();
        ArrayList<Periode> datoPar = new ArrayList<>();
        for (LocalDate localDate : startDatoer) {
            // fra: Hvis startdato er lik datoFraOgMed, bruk denne, hvis ikke, bruk første datoen i mnd.
            LocalDate fra = localDate.equals(datoFraOgMed) ? localDate : førsteDatoIMnd(localDate);
            // til: Hvis siste dag i mnd. er mindre enn datoTilOgMed, bruk siste dag i mnd, ellers bruk datoTilOgMed
            LocalDate til = sisteDatoIMnd(localDate).isBefore(datoTilOgMed) ? sisteDatoIMnd(localDate) : datoTilOgMed;

            datoPar.addAll(splittHvisNyttÅr(fra, til));
        }
        // Legg til siste periode hvis den ikke kom med i loopen
        if (datoPar.get(datoPar.size() - 1).getSlutt() != datoTilOgMed) {
            datoPar.addAll(splittHvisNyttÅr(førsteDatoIMnd(datoTilOgMed), datoTilOgMed));
        }
        return datoPar;
    }

    private static LocalDate førsteDatoIMnd(LocalDate dato) {
        return LocalDate.of(dato.getYear(), dato.getMonth(), 01);
    }

    private static List<Periode> splittHvisNyttÅr (LocalDate fraDato, LocalDate tilDato) {
        if (fraDato.getYear() != tilDato.getYear()) {
            Periode datoPar1 = new Periode(fraDato, fraDato.withMonth(12).withDayOfMonth(31)); //TODO IKKE TESTET
            Periode datoPar2 = new Periode(tilDato.withMonth(1).withDayOfMonth(1), tilDato); //TODO IKKE TESTET
            return List.of(datoPar1, datoPar2);//TODO IKKE TESTET
        } else {
            return List.of(new Periode(fraDato, tilDato));
        }
    }


}
