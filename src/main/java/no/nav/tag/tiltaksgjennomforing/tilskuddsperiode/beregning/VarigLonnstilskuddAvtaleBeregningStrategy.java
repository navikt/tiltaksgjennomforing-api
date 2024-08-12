package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;

public class VarigLonnstilskuddAvtaleBeregningStrategy extends GenerellLonnstilskuddAvtaleBeregningStrategy {

    public void reberegnTotalIAvtale(Avtale avtale) {
        super.reberegnTotalIAvtale(avtale);
       regnUtDatoOgSumRedusert(avtale);
    }

}
