package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.FirearigLonnstilskuddBeregningStrategy;

import java.util.Map;

public class FirearigLonnstilskuddAvtaleInnholdStrategy extends LonnstilskuddAvtaleInnholdStrategy<FirearigLonnstilskuddBeregningStrategy> {
    public FirearigLonnstilskuddAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold, new FirearigLonnstilskuddBeregningStrategy());
    }

    @Override
    public Map<String, Object> alleFelterSomMåFyllesUt() {
        Map<String, Object> felterSomMåFyllesUt = super.alleFelterSomMåFyllesUt();
        felterSomMåFyllesUt.put(AvtaleInnhold.Fields.lonnstilskuddFormaal, avtaleInnhold.getLonnstilskuddFormaal());
        return felterSomMåFyllesUt;
    }

    @Override
    public void endre(EndreAvtale endreAvtale) {
        Integer lonnstilskuddprosentVedStart = beregningStrategy.getProsentForForstePeriode();
        avtaleInnhold.setLonnstilskuddProsent(lonnstilskuddprosentVedStart);
        avtaleInnhold.setLonnstilskuddFormaal(endreAvtale.getLonnstilskuddFormaal());
        super.endre(endreAvtale);
    }
}
