package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.VarigLonnstilskuddAvtaleBeregningStrategy;

import java.util.Map;

public class VarigLonnstilskuddAvtaleInnholdStrategy extends LonnstilskuddAvtaleInnholdStrategy {

    private VarigLonnstilskuddAvtaleBeregningStrategy beregningStrategy;

    public VarigLonnstilskuddAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
        beregningStrategy =  new VarigLonnstilskuddAvtaleBeregningStrategy();
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
        beregningStrategy.reberegnTotal(avtale);
    }

    @Override
    public void reUtregnRedusertProsentOgSum() {
        regnUtDatoOgSumRedusert();
    }

    private void regnUtDatoOgSumRedusert() {
        beregningStrategy.regnUtDatoOgSumRedusert(avtaleInnhold.getAvtale());
    }

}
