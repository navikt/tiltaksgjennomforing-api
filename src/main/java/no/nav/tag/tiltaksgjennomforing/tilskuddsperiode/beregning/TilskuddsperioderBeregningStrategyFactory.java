package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;

import java.util.Optional;


public interface TilskuddsperioderBeregningStrategyFactory {
    static Optional<LonnstilskuddAvtaleBeregningStrategy> create(Tiltakstype tiltakstype){
        return switch (tiltakstype) {
            default -> Optional.empty();
            case MIDLERTIDIG_LONNSTILSKUDD -> Optional.of(new MidlertidigLonnstilskuddAvtaleBeregningStrategy());
            case VARIG_LONNSTILSKUDD -> Optional.of(new VarigLonnstilskuddAvtaleBeregningStrategy());
            case SOMMERJOBB -> Optional.of(new GenerellLonnstilskuddAvtaleBeregningStrategy());
            case VTAO -> Optional.of(new VTAOLonnstilskuddAvtaleBeregningStrategy());
        };
    }
}
