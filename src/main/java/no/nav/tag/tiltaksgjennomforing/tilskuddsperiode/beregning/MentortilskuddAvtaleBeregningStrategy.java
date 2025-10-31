package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;


import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.MentorTilskuddsperioderToggle;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.fikseLøpenumre;

public class MentortilskuddAvtaleBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {
    int MAKS_PROSENT = 100;

    @Override
    public void reberegnTotal(Avtale avtale) {
        AvtaleInnhold innhold = avtale.getGjeldendeInnhold();
        if (innhold.getMentorAntallTimer() == null || innhold.getMentorTimelonn() == null) {
            return;
        }
        var maanedslonn = Double.valueOf(innhold.getMentorTimelonn() * innhold.getMentorAntallTimer()).intValue();
        innhold.setManedslonn(maanedslonn);
        BigDecimal feriepengerBelop = getFeriepengerBelop(innhold.getFeriepengesats(), maanedslonn);
        BigDecimal obligTjenestepensjon = getBeregnetOtpBelop(
            toBigDecimal(innhold.getOtpSats()),
            innhold.getManedslonn(),
            feriepengerBelop
        );
        BigDecimal arbeidsgiveravgiftBelop = getArbeidsgiverAvgift(
            innhold.getManedslonn(), feriepengerBelop, obligTjenestepensjon,
            innhold.getArbeidsgiveravgift()
        );
        Integer sumLonnsutgifter = getSumLonnsutgifter(
            innhold.getManedslonn(),
            feriepengerBelop,
            obligTjenestepensjon,
            arbeidsgiveravgiftBelop
        );
        innhold.setFeriepengerBelop(convertBigDecimalToInt(feriepengerBelop));
        innhold.setOtpBelop(convertBigDecimalToInt(obligTjenestepensjon));
        innhold.setArbeidsgiveravgiftBelop(convertBigDecimalToInt(arbeidsgiveravgiftBelop));
        innhold.setSumLonnsutgifter(sumLonnsutgifter);
        innhold.setSumLonnstilskudd(sumLonnsutgifter);
    }

    @Override
    public boolean nødvendigeFelterErUtfylt(Avtale avtale) {
        AvtaleInnhold innhold = avtale.getGjeldendeInnhold();
        return Utils.erIkkeTomme(
            innhold.getStartDato(),
            innhold.getSluttDato(),
            innhold.getMentorAntallTimer(),
            innhold.getMentorTimelonn(),
            innhold.getFeriepengesats(),
            innhold.getArbeidsgiveravgift(),
            innhold.getOtpSats(),
            innhold.getSumLonnstilskudd()
        );
    }


    @Override
    public List<TilskuddPeriode> genererNyeTilskuddsperioder(Avtale avtale) {
        if (!MentorTilskuddsperioderToggle.isEnabled()) {
            return Collections.emptyList();
        }

        if (avtale.erAvtaleInngått()) {
            return Collections.emptyList();
        }
        if (!nødvendigeFelterErUtfylt(avtale)) {
            return Collections.emptyList();
        }
        AvtaleInnhold innhold = avtale.getGjeldendeInnhold();

        LocalDate start = innhold.getStartDato();
        LocalDate slutt = innhold.getSluttDato();

        List<TilskuddPeriode> perioder = beregnTilskuddsperioderForAvtale(
            UUID.randomUUID(),
            avtale.getTiltakstype(),
            innhold.getSumLonnstilskudd(),
            start,
            slutt,
            MAKS_PROSENT,
            null,
            avtale.getGjeldendeInnhold().getManedslonn100pst()
        );
        perioder.forEach(p -> p.setAvtale(avtale));
        perioder.forEach(p -> p.setEnhet(innhold.getEnhetKostnadssted()));
        perioder.forEach(p -> p.setEnhetsnavn(innhold.getEnhetsnavnKostnadssted()));
        fikseLøpenumre(perioder, 1);
        return perioder;
    }

    @Override
    public Integer beregnTilskuddsbeløpForPeriode(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
        if (!nødvendigeFelterErUtfylt(avtale)) {
            return null;
        }
        AvtaleInnhold innhold = avtale.getGjeldendeInnhold();
        return LonnstilskuddAvtaleBeregningStrategy.beløpForPeriode(
            startDato,
            sluttDato,
            innhold.getSumLonnstilskudd()
        );
    }
}
