package no.nav.tag.tiltaksgjennomforing.avtale.tilskuddsperiodeBeregningStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.*;

import java.time.LocalDate;
import java.util.List;


public interface LonnstilskuddAvtaleBeregningStrategy<T> {
    void genererNyeTilskuddsperioder(Avtale avtale);
    List<TilskuddPeriode> beregnForPeriode(Avtale avtale, LocalDate startDato, LocalDate sluttDato);
    void endre(Avtale avtale,EndreTilskuddsberegning endreTilskuddsberegning);
    void beregnTotal(Avtale avtale);
    void forleng(Avtale avtale, LocalDate gammelSluttDato, LocalDate nySluttDato);
}
