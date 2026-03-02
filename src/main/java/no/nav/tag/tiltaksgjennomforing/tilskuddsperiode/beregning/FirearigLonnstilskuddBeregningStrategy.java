package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public class FirearigLonnstilskuddBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {

    @Override
    public List<TilskuddPeriode> beregnTilskuddsperioderForAvtale(
        Avtale avtale,
        LocalDate datoFraOgMed,
        LocalDate datoTilOgMed
    ) {
        return Periode.av(datoFraOgMed, datoTilOgMed).split(getDatoerForReduksjon(avtale))
            .stream()
            .flatMap(periode -> BeregningStrategy.lagPeriode(periode.getStart(), periode.getSlutt())
                .stream()
                .map(datoPar -> {
                    int prosentForPeriode = getProsentForPeriode(avtale, datoPar);
                    return lagTilskuddsperiode(
                        avtale,
                        datoPar,
                        getSumLonnsTilskudd(avtale.getGjeldendeInnhold().getSumLonnstilskudd(), prosentForPeriode),
                        prosentForPeriode
                    );
                }))
            .toList();

    }

    private int getProsentForPeriode(Avtale avtale, Periode tilskuddsperiode) {
        int antallAar = Period.between(avtale.getGjeldendeInnhold().getStartDato(), tilskuddsperiode.getSlutt().minusDays(1)).getYears();

        return switch (antallAar) {
            case 0 -> 70;
            case 1 -> 60;
            case 2 -> 50;
            case 3 -> 35;
            default -> throw new IllegalStateException("Uventet antall år: " + antallAar);
        };
    }

    private LocalDate[] getDatoerForReduksjon(Avtale avtale) {
        LocalDate startDato = avtale.getGjeldendeInnhold().getStartDato();

        return new LocalDate[] {
            startDato.plusYears(1),
            startDato.plusYears(2),
            startDato.plusYears(3)
        };
    }

    public void sjekktilskuddsprosentSats(Integer lonnstilskuddProsent) {
        if (lonnstilskuddProsent != null && !List.of(70, 60, 50, 35).contains(lonnstilskuddProsent)) {
            throw new FeilLonnstilskuddsprosentException();
        }
    }

}
