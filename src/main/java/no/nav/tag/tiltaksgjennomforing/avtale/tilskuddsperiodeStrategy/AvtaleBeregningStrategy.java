package no.nav.tag.tiltaksgjennomforing.avtale.tilskuddsperiodeStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.*;

import java.time.LocalDate;
import java.util.List;


public interface AvtaleBeregningStrategy {
    static AvtaleBeregningStrategy create(Tiltakstype tiltakstype){
        return switch (tiltakstype) {
            case ARBEIDSTRENING, MENTOR -> new BaseAvtaleBeregningStrategy();
            case MIDLERTIDIG_LONNSTILSKUDD -> new MidlertidigAvtaleBeregningStrategy();
            case VARIG_LONNSTILSKUDD -> new VarigAvtaleBeregningStrategy();
            case INKLUDERINGSTILSKUDD -> new BaseAvtaleBeregningStrategy();
            case SOMMERJOBB -> new BaseAvtaleBeregningStrategy();
            case VTAO -> new VTAOAvtaleBeregningStrategy();
        };
    }

    void generer(Avtale avtale);
    List<TilskuddPeriode> beregn(Avtale avtale, LocalDate startDato, LocalDate sluttDato);
    void endre(Avtale avtale,EndreTilskuddsberegning endreTilskuddsberegning);
    void total(Avtale avtale);
    void forleng(Avtale avtale, LocalDate gammelSluttDato, LocalDate nySluttDato);
}
