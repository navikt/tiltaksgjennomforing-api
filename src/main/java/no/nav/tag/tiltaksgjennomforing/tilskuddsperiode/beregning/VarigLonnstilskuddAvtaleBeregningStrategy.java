package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class VarigLonnstilskuddAvtaleBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {
    public static final int TILSKUDDSPROSENT_MAKS = 75;
    public static final int TILSKUDDSPROSENT_REDUSERT_MAKS = 67;

    @Override
    public Integer getProsentForPeriode(Avtale avtale, AvtaleInnhold avtaleInnhold, Periode periode) {
        Optional<LocalDate> datoForRedusertProsent = getDatoerForReduksjon(avtale, avtaleInnhold).stream().findFirst();

        boolean erRedusert = datoForRedusertProsent
            .map(dato -> !periode.getSlutt().isBefore(dato))
            .orElse(false);

        int lonnstilskuddProsent = avtaleInnhold.getLonnstilskuddProsent() != null ? avtaleInnhold.getLonnstilskuddProsent() : 0;

        return Math.min(lonnstilskuddProsent, erRedusert ? TILSKUDDSPROSENT_REDUSERT_MAKS : TILSKUDDSPROSENT_MAKS);
    }

    @Override
    public List<LocalDate> getDatoerForReduksjon(Avtale avtale, AvtaleInnhold avtaleInnhold) {
        LocalDate startDato = avtaleInnhold.getStartDato();
        LocalDate sluttDato = avtaleInnhold.getSluttDato();
        if (startDato == null || sluttDato == null) {
            return Collections.emptyList();
        }

        Integer lonnstilskuddProsent = avtaleInnhold.getLonnstilskuddProsent();
        if (lonnstilskuddProsent != null && lonnstilskuddProsent <= TILSKUDDSPROSENT_REDUSERT_MAKS) {
            return Collections.emptyList();
        }

        LocalDate datoForReduksjon = startDato.plusYears(1);
        if (datoForReduksjon.isAfter(sluttDato)) {
            return Collections.emptyList();
        }

        return List.of(datoForReduksjon);
    }

    @Override
    public void endreBeregning(Avtale avtale, EndreTilskuddsberegning endreTilskuddsberegning) {
        AvtaleInnhold avtaleInnhold = avtale.getGjeldendeInnhold();
        avtaleInnhold.setLonnstilskuddProsent(endreTilskuddsberegning.getLonnstilskuddProsent());
        super.endreBeregning(avtale, endreTilskuddsberegning);
    }

}
