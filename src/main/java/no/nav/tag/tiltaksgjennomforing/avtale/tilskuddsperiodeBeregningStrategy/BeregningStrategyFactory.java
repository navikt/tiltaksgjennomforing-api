package no.nav.tag.tiltaksgjennomforing.avtale.tilskuddsperiodeBeregningStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.EndreTilskuddsberegning;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.*;


public interface BeregningStrategyFactory {
    static LonnstilskuddAvtaleBeregningStrategy create(Tiltakstype tiltakstype){
        if (erEnTiltakstypeSomIkkeStotterTilskuddsperioder(tiltakstype)) return null;
        return switch (tiltakstype) {
            default -> throw new FeilkodeException(Feilkode.AVTALE_STOTTER_IKKE_TILSKUDDSPERIODER);
            case MIDLERTIDIG_LONNSTILSKUDD -> new MidlertidigLonnstilskuddAvtaleBeregningStrategy();
            case VARIG_LONNSTILSKUDD -> new VarigLonnstilskuddAvtaleBeregningStrategy();
            case SOMMERJOBB -> new GenerellLonnstilskuddAvtaleBeregningStrategy();
            case VTAO -> new VTAOLonnstilskuddAvtaleBeregningStrategy();
        };
    }
    private static boolean erEnTiltakstypeSomIkkeStotterTilskuddsperioder(Tiltakstype tiltakstype){
        return EnumSet.of(ARBEIDSTRENING, MENTOR,INKLUDERINGSTILSKUDD).contains(tiltakstype);
    }
}
