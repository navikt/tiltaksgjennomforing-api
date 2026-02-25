package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.FirearigLonnstilskuddBeregningStrategy;

public class FirearigLonnstilskuddAvtaleInnholdStrategy extends LonnstilskuddAvtaleInnholdStrategy {

    private FirearigLonnstilskuddBeregningStrategy beregningStrategy;

    public FirearigLonnstilskuddAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
        beregningStrategy = new FirearigLonnstilskuddBeregningStrategy();
    }
}
