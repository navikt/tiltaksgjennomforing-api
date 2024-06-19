package no.nav.tag.tiltaksgjennomforing.avtale.tilskuddsperiodeBeregningStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.*;

import java.time.LocalDate;
import java.util.List;


public interface LonnstilskuddAvtaleBeregningStrategy {
    static LonnstilskuddAvtaleBeregningStrategy create(Tiltakstype tiltakstype){
        return switch (tiltakstype) {
            case ARBEIDSTRENING, MENTOR -> new GenerellLonnstilskuddAvtaleBeregningStrategy();
            case MIDLERTIDIG_LONNSTILSKUDD -> new MidlertidigLonnstilskuddAvtaleBeregningStrategy();
            case VARIG_LONNSTILSKUDD -> new VarigLonnstilskuddAvtaleBeregningStrategy();
            case INKLUDERINGSTILSKUDD -> new GenerellLonnstilskuddAvtaleBeregningStrategy();
            case SOMMERJOBB -> new GenerellLonnstilskuddAvtaleBeregningStrategy();
            case VTAO -> new VTAOLonnstilskuddAvtaleBeregningStrategy();
        };
    }

    void generer(Avtale avtale);
    List<TilskuddPeriode> beregn(Avtale avtale, LocalDate startDato, LocalDate sluttDato);
    void endre(Avtale avtale,EndreTilskuddsberegning endreTilskuddsberegning);
    void total(Avtale avtale);
    void forleng(Avtale avtale, LocalDate gammelSluttDato, LocalDate nySluttDato);
}
