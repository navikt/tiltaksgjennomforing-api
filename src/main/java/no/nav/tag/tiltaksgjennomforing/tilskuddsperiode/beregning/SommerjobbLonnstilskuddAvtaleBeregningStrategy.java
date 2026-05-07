package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;


import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class SommerjobbLonnstilskuddAvtaleBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {
    public static int TILSKUDDSPROSENT_MAKS = 75;
    public static int TILSKUDDSPROSENT_MIN = 50;

    @Override
    public Integer getProsentForPeriode(Avtale avtale, AvtaleInnhold avtaleInnhold, Periode periode) {
        return avtaleInnhold.getLonnstilskuddProsent();
    }

    @Override
    public List<LocalDate> getDatoerForReduksjon(Avtale avtale, AvtaleInnhold avtaleInnhold) {
        return Collections.emptyList();
    }

}
