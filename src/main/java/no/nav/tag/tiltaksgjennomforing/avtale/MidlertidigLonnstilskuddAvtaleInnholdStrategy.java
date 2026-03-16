package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.MidlertidigLonnstilskuddAvtaleBeregningStrategy;

public class MidlertidigLonnstilskuddAvtaleInnholdStrategy extends LonnstilskuddAvtaleInnholdStrategy<MidlertidigLonnstilskuddAvtaleBeregningStrategy> {
    public MidlertidigLonnstilskuddAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold, new MidlertidigLonnstilskuddAvtaleBeregningStrategy());
    }

    @Override
    public void endre(EndreAvtale endreAvtale) {
        Avtale avtale = avtaleInnhold.getAvtale();
        Integer lonstilskuddprosentVedStart = beregningStrategy.getProsentForForstePeriode(avtale);
        avtaleInnhold.setLonnstilskuddProsent(lonstilskuddprosentVedStart);
        super.endre(endreAvtale);
    }

}
