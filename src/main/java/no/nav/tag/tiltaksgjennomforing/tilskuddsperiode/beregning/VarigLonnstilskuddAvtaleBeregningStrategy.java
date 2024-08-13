package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;

public class VarigLonnstilskuddAvtaleBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {

    public void reberegnTotal(Avtale avtale) {
        super.reberegnTotal(avtale);
       regnUtDatoOgSumRedusert(avtale);
    }

}
