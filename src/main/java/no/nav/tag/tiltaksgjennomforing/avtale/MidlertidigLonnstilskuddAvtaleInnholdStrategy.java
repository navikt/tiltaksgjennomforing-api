package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.MidlertidigLonnstilskuddAvtaleBeregningStrategy;

import java.util.Map;

public class MidlertidigLonnstilskuddAvtaleInnholdStrategy extends LonnstilskuddAvtaleInnholdStrategy<MidlertidigLonnstilskuddAvtaleBeregningStrategy> {
    public MidlertidigLonnstilskuddAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold, new MidlertidigLonnstilskuddAvtaleBeregningStrategy(avtaleInnhold.getAvtale()));
    }

    @Override
    public Map<String, Object> alleFelterSomMåFyllesUt() {
        Map<String, Object> felterSomMåFyllesUt = super.alleFelterSomMåFyllesUt();
        felterSomMåFyllesUt.put(AvtaleInnhold.Fields.lonnstilskuddFormaal, avtaleInnhold.getLonnstilskuddFormaal());
        return felterSomMåFyllesUt;
    }

    @Override
    public void endre(EndreAvtale endreAvtale) {
        Avtale avtale = avtaleInnhold.getAvtale();
        Integer lonnstilskuddprosentVedStart = beregningStrategy.getProsentForForstePeriode();
        avtaleInnhold.setLonnstilskuddProsent(lonnstilskuddprosentVedStart);
        avtaleInnhold.setLonnstilskuddFormaal(endreAvtale.getLonnstilskuddFormaal());
        super.endre(endreAvtale);
    }

}
