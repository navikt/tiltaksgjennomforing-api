package no.nav.tag.tiltaksgjennomforing.avtale.tilskuddsperiodeBeregningStrategy;

import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.ARBEIDSTRENING;
import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.MENTOR;
import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.INKLUDERINGSTILSKUDD;


public interface LonnstilskuddAvtaleBeregningStrategy {
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
    void generer(Avtale avtale);
    List<TilskuddPeriode> beregn(Avtale avtale, LocalDate startDato, LocalDate sluttDato);
    void endre(Avtale avtale,EndreTilskuddsberegning endreTilskuddsberegning);
    void total(Avtale avtale);
    void forleng(Avtale avtale, LocalDate gammelSluttDato, LocalDate nySluttDato);
}
