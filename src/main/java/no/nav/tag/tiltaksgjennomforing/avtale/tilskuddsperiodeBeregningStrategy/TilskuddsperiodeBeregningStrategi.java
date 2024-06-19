package no.nav.tag.tiltaksgjennomforing.avtale.tilskuddsperiodeBeregningStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.*;

import java.time.LocalDate;
import java.util.List;


public interface TilskuddsperiodeBeregningStrategi {
    static TilskuddsperiodeBeregningStrategi create(Tiltakstype tiltakstype){
        return switch (tiltakstype) {
            case ARBEIDSTRENING, MENTOR -> new GenerellTilskuddsperiodeBeregningStrategi();
            case MIDLERTIDIG_LONNSTILSKUDD -> new MidlertidigTilskuddsperiodeBeregningStrategi();
            case VARIG_LONNSTILSKUDD -> new VarigTilskuddsperiodeBeregningStrategi();
            case INKLUDERINGSTILSKUDD -> new GenerellTilskuddsperiodeBeregningStrategi();
            case SOMMERJOBB -> new GenerellTilskuddsperiodeBeregningStrategi();
            case VTAO -> new VTAOTilskuddsperiodeBeregningStrategi();
        };
    }

    void generer(Avtale avtale);
    List<TilskuddPeriode> beregn(Avtale avtale, LocalDate startDato, LocalDate sluttDato);
    void endre(Avtale avtale,EndreTilskuddsberegning endreTilskuddsberegning);
    void total(Avtale avtale);
    void forleng(Avtale avtale, LocalDate gammelSluttDato, LocalDate nySluttDato);
}
