package no.nav.tag.tiltaksgjennomforing.avtale;

import java.time.LocalDate;

import no.nav.tag.tiltaksgjennomforing.avtale.tilskuddsperiodeBeregningStrategy.LonnstilskuddAvtaleBeregningStrategy;
import no.nav.tag.tiltaksgjennomforing.avtale.tilskuddsperiodeBeregningStrategy.VarigLonnstilskuddAvtaleBeregningStrategy;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;

import java.util.Map;

import static no.nav.tag.tiltaksgjennomforing.avtale.tilskuddsperiodeBeregningStrategy.VarigLonnstilskuddAvtaleBeregningStrategy.GRENSE_68_PROSENT_ETTER_12_MND;

public class VarigLonnstilskuddAvtaleInnholdStrategy extends LonnstilskuddAvtaleInnholdStrategy {

    private VarigLonnstilskuddAvtaleBeregningStrategy varigLonnstilskuddAvtaleBeregningStrategy;

    public VarigLonnstilskuddAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold){
        super(avtaleInnhold);
        varigLonnstilskuddAvtaleBeregningStrategy =  (VarigLonnstilskuddAvtaleBeregningStrategy) LonnstilskuddAvtaleBeregningStrategy.create(this.avtaleInnhold.getAvtale().getTiltakstype());
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
        LonnstilskuddAvtaleBeregningStrategy.create(avtale.getTiltakstype()).total(avtale);
    }

    @Override
    public void reUtregnRedusertProsentOgSum() {
        regnUtDatoOgSumRedusert();
    }

    private LocalDate getDatoForRedusertProsent(LocalDate startDato, LocalDate sluttDato, Integer lonnstilskuddprosent) {
        if (startDato == null || sluttDato == null || lonnstilskuddprosent == null) {
            return null;
        }
        if (startDato.plusYears(1).minusDays(1).isBefore(sluttDato)) {
            return startDato.plusYears(1);
        }
        return null;
    }
    private void regnUtDatoOgSumRedusert() {
        varigLonnstilskuddAvtaleBeregningStrategy.regnUtDatoOgSumRedusert(avtaleInnhold.getAvtale());
    }

}
