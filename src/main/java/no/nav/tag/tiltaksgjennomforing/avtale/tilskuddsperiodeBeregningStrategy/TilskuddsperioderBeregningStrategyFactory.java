package no.nav.tag.tiltaksgjennomforing.avtale.tilskuddsperiodeBeregningStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.*;


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
