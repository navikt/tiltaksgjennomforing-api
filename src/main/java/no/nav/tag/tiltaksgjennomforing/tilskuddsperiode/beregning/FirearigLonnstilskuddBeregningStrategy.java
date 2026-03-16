package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class FirearigLonnstilskuddBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {
    public static final int TILSKUDDSPROSENT_FORSTE_AAR = 70;
    public static final int TILSKUDDSPROSENT_ANDRE_AAR = 60;
    public static final int TILSKUDDSPROSENT_TREDJE_AAR = 50;
    public static final int TILSKUDDSPROSENT_FJERDE_AAR = 35;

    public Integer getProsentForForstePeriode() {
        return TILSKUDDSPROSENT_FORSTE_AAR;
    }

    @Override
    public Integer getProsentForPeriode(Avtale avtale, Periode periode) {
        LocalDate avtaleStart = avtale.getGjeldendeInnhold().getStartDato();
        LocalDate tilskuddSlutt = periode.getSlutt();

        if (tilskuddSlutt.isBefore(avtaleStart.plusYears(1))) {
            return TILSKUDDSPROSENT_FORSTE_AAR;
        }
        if (tilskuddSlutt.isBefore(avtaleStart.plusYears(2))) {
            return TILSKUDDSPROSENT_ANDRE_AAR;
        }
        if (tilskuddSlutt.isBefore(avtaleStart.plusYears(3))) {
            return TILSKUDDSPROSENT_TREDJE_AAR;
        }
        if (tilskuddSlutt.isBefore(avtaleStart.plusYears(4))) {
            return TILSKUDDSPROSENT_FJERDE_AAR;
        }

        throw new IllegalStateException("Tilskuddsperiode avsluttes mer enn 4 år etter startdato");
    }

    @Override
    public List<LocalDate> getDatoerForReduksjon(Avtale avtale) {
        LocalDate startDato = avtale.getGjeldendeInnhold().getStartDato();
        LocalDate sluttDato = avtale.getGjeldendeInnhold().getSluttDato();

        if (startDato == null || sluttDato == null) {
            return Collections.emptyList();
        }

        List<LocalDate> datoerForReduksjon = List.of(
            startDato.plusYears(1),
            startDato.plusYears(2),
            startDato.plusYears(3)
        );

        return datoerForReduksjon.stream()
            .filter(dato -> !dato.isAfter(sluttDato))
            .toList();
    }

}
