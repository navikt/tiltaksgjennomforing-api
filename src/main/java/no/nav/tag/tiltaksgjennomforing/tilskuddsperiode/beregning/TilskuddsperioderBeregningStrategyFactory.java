package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;

public interface TilskuddsperioderBeregningStrategyFactory {
    static LonnstilskuddAvtaleBeregningStrategy create(Tiltakstype tiltakstype){
        return switch (tiltakstype) {
            case ARBEIDSTRENING,INKLUDERINGSTILSKUDD -> new IkkeLonnstilskuddAvtaleBeregningStrategy();
            case MENTOR -> new MentortilskuddAvtaleBeregningStrategy();
            case MIDLERTIDIG_LONNSTILSKUDD ->new MidlertidigLonnstilskuddAvtaleBeregningStrategy();
            case VARIG_LONNSTILSKUDD -> new VarigLonnstilskuddAvtaleBeregningStrategy();
            case SOMMERJOBB -> new SommerjobbLonnstilskuddAvtaleBeregningStrategy();
            case VTAO -> new VTAOLonnstilskuddAvtaleBeregningStrategy();
        };
    }
}
