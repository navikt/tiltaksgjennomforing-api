package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;


import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class SommerjobbLonnstilskuddAvtaleBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {

    @Override
    public Integer getProsentForPeriode(Avtale avtale, Periode tilskuddsperiode) {
        return avtale.getGjeldendeInnhold().getLonnstilskuddProsent();
    }

    @Override
    public List<LocalDate> getDatoerForReduksjon(Avtale avtale) {
        return Collections.emptyList();
    }

}
