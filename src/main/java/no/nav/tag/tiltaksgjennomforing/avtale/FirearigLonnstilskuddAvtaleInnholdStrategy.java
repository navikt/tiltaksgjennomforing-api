package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.FirearigLonnstilskuddBeregningStrategy;

public class FirearigLonnstilskuddAvtaleInnholdStrategy extends LonnstilskuddAvtaleInnholdStrategy<FirearigLonnstilskuddBeregningStrategy> {
    public FirearigLonnstilskuddAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold, new FirearigLonnstilskuddBeregningStrategy());
    }

    @Override
    public void endre(EndreAvtale endreAvtale) {
        Integer lonstilskuddprosentVedStart = beregningStrategy.getProsentForForstePeriode();
        avtaleInnhold.setLonnstilskuddProsent(lonstilskuddprosentVedStart);
        super.endre(endreAvtale);
    }
}
