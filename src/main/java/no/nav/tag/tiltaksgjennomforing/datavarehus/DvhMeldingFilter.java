package no.nav.tag.tiltaksgjennomforing.datavarehus;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;

public class DvhMeldingFilter {
    public static boolean skalTilDatavarehus(Avtale avtale) {
        return avtale.erGodkjentAvVeileder() && avtale.getTiltakstype() == Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD;
    }
}
