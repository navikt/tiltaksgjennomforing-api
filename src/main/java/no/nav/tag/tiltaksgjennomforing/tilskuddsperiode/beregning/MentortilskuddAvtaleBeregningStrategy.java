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
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.utils.Utils.fikseLøpenumre;

public class MentorLonnstilskuddAvtaleBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {
    int MAKS_PROSENT = 100;

    @Override
    public void reberegnTotal(Avtale avtale) {
        AvtaleInnhold innhold = avtale.getGjeldendeInnhold();
        if (innhold.getMentorAntallTimer() != null
            && innhold.getMentorTimelonn() != null) {
            int manedslonn = beregnMånedligTilskudd(
                innhold.getMentorAntallTimer(),
                innhold.getMentorTimelonn());
            innhold.setManedslonn(manedslonn);
            innhold.setLonnstilskuddProsent(MAKS_PROSENT);
        super.reberegnTotal(avtale);
        }
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
            innhold.getStillingprosent(),
            innhold.getManedslonn100pst()
        );
    }


    @Override
    public List<TilskuddPeriode> genererNyeTilskuddsperioder(Avtale avtale) {
        if (!MentorTilskuddsperioderToggle.isEnabled()){
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
        LocalDate redusertFra = innhold.getDatoForRedusertProsent();

//        Integer månedligTilskudd = beregnMånedligTilskudd(innhold.getMentorAntallTimer(), innhold.getMentorTimelonn());

        List<TilskuddPeriode> perioder = beregnTilskuddsperioderForAvtale(
            UUID.randomUUID(),
            avtale.getTiltakstype(),
            avtale.getGjeldendeInnhold().getManedslonn100pst(),
            start,
            slutt,
            MAKS_PROSENT,
            redusertFra,
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
        if (!nødvendigeFelterErUtfylt(avtale) || startDato == null || sluttDato == null) {
            return null;
        }
        AvtaleInnhold innhold = avtale.getGjeldendeInnhold();
        int månedsLonn = beregnMånedligTilskudd(innhold.getMentorAntallTimer(), innhold.getMentorTimelonn());
        return LonnstilskuddAvtaleBeregningStrategy.beløpForPeriode(startDato, sluttDato, månedsLonn);
    }

    private Integer beregnMånedligTilskudd(double antalTimerIMåned, double timelonn) {
        BigDecimal månedslonnBeregningFraTimerMedTimelonn = BigDecimal.valueOf(antalTimerIMåned).multiply(BigDecimal.valueOf(timelonn));
        return månedslonnBeregningFraTimerMedTimelonn.setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
