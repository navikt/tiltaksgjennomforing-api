package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;

public interface TilskuddsperioderBeregningStrategyFactory {
    static LonnstilskuddAvtaleBeregningStrategy create(Tiltakstype tiltakstype){
        return switch (tiltakstype) {
            case MENTOR,ARBEIDSTRENING,INKLUDERINGSTILSKUDD -> new IkkeLonnstilskuddAvtaleBeregningStrategy();
            case MIDLERTIDIG_LONNSTILSKUDD ->new MidlertidigLonnstilskuddAvtaleBeregningStrategy();
            case VARIG_LONNSTILSKUDD, LANGVARIG_LONNSTILSKUDD -> new VarigLonnstilskuddAvtaleBeregningStrategy();
            case SOMMERJOBB -> new SommerjobbLonnstilskuddAvtaleBeregningStrategy();
            case VTAO -> new VTAOLonnstilskuddAvtaleBeregningStrategy();
        };
    }
}
