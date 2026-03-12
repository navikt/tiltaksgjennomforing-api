package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class FirearigLonnstilskuddBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {
    private static final int TILSKUDDSPROSENT_FORSTE_AAR = 70;
    private static final int TILSKUDDSPROSENT_ANDRE_AAR = 60;
    private static final int TILSKUDDSPROSENT_TREDJE_AAR = 50;
    private static final int TILSKUDDSPROSENT_FJERDE_AAR = 35;

    @Override
    public Integer getProsentForPeriode(Avtale avtale, Periode tilskuddsperiode) {
        LocalDate startDato = avtale.getGjeldendeInnhold().getStartDato();
        LocalDate periodeStart = tilskuddsperiode.getStart();
        if (periodeStart.isBefore(startDato.plusYears(1))) {
            return TILSKUDDSPROSENT_FORSTE_AAR;
        } else if (periodeStart.isBefore(startDato.plusYears(2))) {
            return TILSKUDDSPROSENT_ANDRE_AAR;
        } else if (periodeStart.isBefore(startDato.plusYears(3))) {
            return TILSKUDDSPROSENT_TREDJE_AAR;
        } else if (periodeStart.isBefore(startDato.plusYears(4))) {
            return TILSKUDDSPROSENT_FJERDE_AAR;
        }
        throw new IllegalStateException("Tilskuddsperiode starter mer enn 4 år etter startdato");
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
