package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.VarigLonnstilskuddAvtaleBeregningStrategy;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;

import java.util.Map;

public class VarigLonnstilskuddAvtaleInnholdStrategy extends LonnstilskuddAvtaleInnholdStrategy {

    private VarigLonnstilskuddAvtaleBeregningStrategy varigLonnstilskuddAvtaleBeregningStrategy;

    public VarigLonnstilskuddAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold){
        super(avtaleInnhold);
        varigLonnstilskuddAvtaleBeregningStrategy =  new VarigLonnstilskuddAvtaleBeregningStrategy();
    }

    @Override
    public Map<String, Object> alleFelterSomMåFyllesUt() {
        Map<String, Object> felterSomMåFyllesUt = super.alleFelterSomMåFyllesUt();
        felterSomMåFyllesUt.put(AvtaleInnhold.Fields.lonnstilskuddProsent, avtaleInnhold.getLonnstilskuddProsent());
        return felterSomMåFyllesUt;
    }

    @Override
    public void endre(EndreAvtale endreAvtale) {
        if (endreAvtale.getLonnstilskuddProsent() != null && (
            endreAvtale.getLonnstilskuddProsent() < 0 || endreAvtale.getLonnstilskuddProsent() > 75)) {
            throw new FeilLonnstilskuddsprosentException();
        }
        avtaleInnhold.setLonnstilskuddProsent(endreAvtale.getLonnstilskuddProsent());
        super.endre(endreAvtale);
    }

    @Override
    public void regnUtTotalLonnstilskudd() {
        Avtale avtale = avtaleInnhold.getAvtale();
        varigLonnstilskuddAvtaleBeregningStrategy.reberegnTotal(avtale);
    }

    @Override
    public void reUtregnRedusertProsentOgSum() {
        regnUtDatoOgSumRedusert();
    }

    private void regnUtDatoOgSumRedusert() {
        varigLonnstilskuddAvtaleBeregningStrategy.regnUtDatoOgSumRedusert(avtaleInnhold.getAvtale());
    }

}
