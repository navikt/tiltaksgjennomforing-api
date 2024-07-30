package no.nav.tag.tiltaksgjennomforing.avtale.tilskuddsperiodeBeregningStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

import static no.nav.tag.tiltaksgjennomforing.utils.DatoUtils.sisteDatoIMnd;
import static no.nav.tag.tiltaksgjennomforing.utils.Utils.erIkkeTomme;

public class MidlertidigLonnstilskuddAvtaleBeregningStrategy implements LonnstilskuddAvtaleBeregningStrategy<MidlertidigLonnstilskuddAvtaleBeregningStrategy> {

    public void genererNyeTilskuddsperioder(Avtale avtale){
        if (avtale.erAvtaleInngått()) {
            throw new FeilkodeException(Feilkode.KAN_IKKE_LAGE_NYE_TILSKUDDSPRIODER_INNGAATT_AVTALE);
        }
        List<TilskuddPeriode> tilskuddsperioder = new ArrayList<>();
        avtale.hentTilskuddsperioder().removeIf(t -> (t.getStatus() == TilskuddPeriodeStatus.UBEHANDLET) || (t.getStatus() == TilskuddPeriodeStatus.BEHANDLET_I_ARENA));
        AvtaleInnhold gjeldendeInnhold = avtale.getGjeldendeInnhold();
        Tiltakstype tiltakstype = avtale.getTiltakstype();

        if (erIkkeTomme(gjeldendeInnhold.getStartDato(), gjeldendeInnhold.getSluttDato(), gjeldendeInnhold.getSumLonnstilskudd())) {
            tilskuddsperioder = beregnForPeriode(avtale,gjeldendeInnhold.getStartDato(), gjeldendeInnhold.getSluttDato());
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

    public void forleng(Avtale avtale, LocalDate gammelSluttDato, LocalDate nySluttDato) {
        Set<TilskuddPeriode> tilskuddPeriode = avtale.hentTilskuddsperioder();

        if (avtale.hentTilskuddsperioder().isEmpty()) {
            return;
        }
        Optional<TilskuddPeriode> periodeMedHøyestLøpenummer = tilskuddPeriode.stream().max(Comparator.comparing(TilskuddPeriode::getLøpenummer));
        TilskuddPeriode sisteTilskuddsperiode = periodeMedHøyestLøpenummer.get();
        if (sisteTilskuddsperiode.getStatus() == TilskuddPeriodeStatus.UBEHANDLET) {
            // Kan utvide siste tilskuddsperiode hvis den er ubehandlet
            tilskuddPeriode.remove(sisteTilskuddsperiode);
            List<TilskuddPeriode> nyeTilskuddperioder = beregnForPeriode(avtale,sisteTilskuddsperiode.getStartDato(), nySluttDato);
            fikseLøpenumre(nyeTilskuddperioder, sisteTilskuddsperiode.getLøpenummer());
            tilskuddPeriode.addAll(nyeTilskuddperioder);
        } else if (sisteTilskuddsperiode.getSluttDato().isBefore(sisteDatoIMnd(sisteTilskuddsperiode.getSluttDato())) && sisteTilskuddsperiode.getStatus() == TilskuddPeriodeStatus.GODKJENT && (!sisteTilskuddsperiode.erRefusjonGodkjent() && !sisteTilskuddsperiode.erUtbetalt())) {
            avtale.annullerTilskuddsperiode(sisteTilskuddsperiode);
            List<TilskuddPeriode> nyeTilskuddperioder = beregnForPeriode(avtale,sisteTilskuddsperiode.getStartDato(), nySluttDato);
            fikseLøpenumre(nyeTilskuddperioder, sisteTilskuddsperiode.getLøpenummer() + 1);
            tilskuddPeriode.addAll(nyeTilskuddperioder);
        } else {
            // Regner ut nye perioder fra gammel avtaleslutt til ny avtaleslutt
            List<TilskuddPeriode> nyeTilskuddperioder = beregnForPeriode(avtale,gammelSluttDato.plusDays(1), nySluttDato);
            fikseLøpenumre(nyeTilskuddperioder, sisteTilskuddsperiode.getLøpenummer() + 1);
            tilskuddPeriode.addAll(nyeTilskuddperioder);
        }
    }
    public void endre(Avtale avtale,EndreTilskuddsberegning endreTilskuddsberegning) {
       AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();
        avtaleInnhold.setArbeidsgiveravgift(endreTilskuddsberegning.getArbeidsgiveravgift());
        avtaleInnhold.setOtpSats(endreTilskuddsberegning.getOtpSats());
        avtaleInnhold.setManedslonn(endreTilskuddsberegning.getManedslonn());
        avtaleInnhold.setFeriepengesats(endreTilskuddsberegning.getFeriepengesats());
        reberegnTotal(avtale);
        regnUtDatoOgSumRedusert(avtale);
        avtale.sendTilbakeTilBeslutter();
        avtale.hentTilskuddsperioder().stream().filter(t -> t.getStatus() == TilskuddPeriodeStatus.UBEHANDLET)
                .forEach(t -> t.setBeløp(beregnTilskuddsbeløp(avtale,t.getStartDato(), t.getSluttDato())));


    }

    private Integer beregnTilskuddsbeløp(Avtale avtale,LocalDate startDato, LocalDate sluttDato) {
        AvtaleInnhold gjeldendeInnhold = avtale.getGjeldendeInnhold();
        return RegnUtTilskuddsperioderForAvtale.beløpForPeriode(startDato,
                sluttDato,
                gjeldendeInnhold.getDatoForRedusertProsent(),
                gjeldendeInnhold.getSumLonnstilskudd(),
                gjeldendeInnhold.getSumLønnstilskuddRedusert());
    }

    public void reberegnTotal(Avtale avtale) {
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
    public List<TilskuddPeriode> beregnForPeriode(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
        AvtaleInnhold gjeldendeInnhold = avtale.getGjeldendeInnhold();

        List<TilskuddPeriode> tilskuddsperioder = RegnUtTilskuddsperioderForAvtale.beregnTilskuddsperioderForAvtale(
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

    /* Reducer? */
    static void fikseLøpenumre(List<TilskuddPeriode> tilskuddperioder, int startPåLøpenummer) {
        for (int i = 0; i < tilskuddperioder.size(); i++) {
            tilskuddperioder.get(i).setLøpenummer(startPåLøpenummer + i);
        }
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
