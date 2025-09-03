package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;


import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
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

    @Override
    public boolean nødvendigeFelterErUtfylt(Avtale avtale) {
        AvtaleInnhold innhold = avtale.getGjeldendeInnhold();
        return Utils.erIkkeTomme(
            innhold.getStartDato(),
            innhold.getSluttDato(),
            innhold.getMentorAntallTimer(),
            innhold.getMentorTimelonn()
        );
    }


    @Override
    public List<TilskuddPeriode> genererNyeTilskuddsperioder(Avtale avtale) {
        if (avtale.erAvtaleInngått()) {
            return Collections.emptyList();
        }
        if (!nødvendigeFelterErUtfylt(avtale)) {
            return Collections.emptyList();
        }
        AvtaleInnhold innhold = avtale.getGjeldendeInnhold();

        int prosent = 100; //innhold.getLonnstilskuddProsent();
        LocalDate start = innhold.getStartDato();
        LocalDate slutt = innhold.getSluttDato();
        LocalDate redusertFra = innhold.getDatoForRedusertProsent();


        BigDecimal timerPerUke = BigDecimal.valueOf(innhold.getMentorAntallTimer());
        BigDecimal timelonn = BigDecimal.valueOf(innhold.getMentorTimelonn());
        BigDecimal brutto = timerPerUke.multiply(timelonn);

        Integer månedligTilskudd = beregnMånedligTilskudd(brutto, prosent);
        Integer månedligTilskuddRedusert = null;
        if (redusertFra != null) {
            int redusertProsent = Math.max(0, prosent - 10);
            månedligTilskuddRedusert = beregnMånedligTilskudd(brutto, redusertProsent);
        }

        List<TilskuddPeriode> perioder = beregnTilskuddsperioderForAvtale(
            UUID.randomUUID(),
            avtale.getTiltakstype(),
            månedligTilskudd,
            start,
            slutt,
            prosent,
            redusertFra,
            månedligTilskuddRedusert
        );
        perioder.forEach(p -> p.setAvtale(avtale));
        perioder.forEach(p -> p.setEnhet(innhold.getEnhetKostnadssted()));
        perioder.forEach(p -> p.setEnhetsnavn(innhold.getEnhetsnavnKostnadssted()));
        fikseLøpenumre(perioder, 1);
        return perioder;
    }

    @Override
    public List<TilskuddPeriode> hentTilskuddsperioderForPeriode(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
        if (startDato == null || sluttDato == null || startDato.isAfter(sluttDato) || !nødvendigeFelterErUtfylt(avtale)) {
            return Collections.emptyList();
        }
        AvtaleInnhold innhold = avtale.getGjeldendeInnhold();

        int prosent = 100; //innhold.getLonnstilskuddProsent();
        LocalDate redusertFra = innhold.getDatoForRedusertProsent();

        BigDecimal brutto = BigDecimal
            .valueOf(innhold.getMentorAntallTimer())
            .multiply(BigDecimal.valueOf(innhold.getMentorTimelonn()));

        Integer månedligTilskudd = beregnMånedligTilskudd(brutto, prosent);
        Integer månedligTilskuddRedusert = null;
        if (redusertFra != null) {
            int redusertProsent = Math.max(0, prosent - 10);
            månedligTilskuddRedusert = beregnMånedligTilskudd(brutto, redusertProsent);
        }

        List<TilskuddPeriode> perioder = beregnTilskuddsperioderForAvtale(
            UUID.randomUUID(),
            avtale.getTiltakstype(),
            månedligTilskudd,
            startDato,
            sluttDato,
            prosent,
            redusertFra,
            månedligTilskuddRedusert
        );
        perioder.forEach(p -> p.setAvtale(avtale));
        perioder.forEach(p -> p.setEnhet(innhold.getEnhetKostnadssted()));
        perioder.forEach(p -> p.setEnhetsnavn(innhold.getEnhetsnavnKostnadssted()));
        return perioder;
    }

    @Override
    public Integer beregnTilskuddsbeløpForPeriode(Avtale avtale, LocalDate startDato, LocalDate sluttDato) {
        if (!nødvendigeFelterErUtfylt(avtale) || startDato == null || sluttDato == null) {
            return null;
        }
        AvtaleInnhold innhold = avtale.getGjeldendeInnhold();

        BigDecimal brutto = BigDecimal
            .valueOf(innhold.getMentorAntallTimer())
            .multiply(BigDecimal.valueOf(innhold.getMentorTimelonn()));

        int prosent = 100; //innhold.getLonnstilskuddProsent();
        Integer månedligTilskudd = beregnMånedligTilskudd(brutto, prosent);

        LocalDate redusertFra = innhold.getDatoForRedusertProsent();
        Integer månedligTilskuddRedusert = null;
        if (redusertFra != null) {
            int redusertProsent = Math.max(0, prosent - 10);
            månedligTilskuddRedusert = beregnMånedligTilskudd(brutto, redusertProsent);
        }


        if (redusertFra == null || sluttDato.isBefore(redusertFra)) {
            return LonnstilskuddAvtaleBeregningStrategy.beløpForPeriode(startDato, sluttDato, månedligTilskudd);
        } else {

            if (månedligTilskuddRedusert == null) {
                return null;
            }
            return LonnstilskuddAvtaleBeregningStrategy.beløpForPeriode(startDato, sluttDato, månedligTilskuddRedusert);
        }
    }

    private Integer beregnMånedligTilskudd(BigDecimal bruttoGrunnlag, int prosent) {
        if (prosent <= 0) {
            return 0;
        }
        BigDecimal tilskudd = bruttoGrunnlag
            .multiply(BigDecimal.valueOf(prosent))
            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        return tilskudd.intValue();
    }
}
