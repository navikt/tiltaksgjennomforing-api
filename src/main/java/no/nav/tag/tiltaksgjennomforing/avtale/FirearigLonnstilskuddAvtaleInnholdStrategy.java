package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.FirearigLonnstilskuddBeregningStrategy;

import java.util.Map;

public class FirearigLonnstilskuddAvtaleInnholdStrategy extends LonnstilskuddAvtaleInnholdStrategy<FirearigLonnstilskuddBeregningStrategy> {
    public FirearigLonnstilskuddAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold, new FirearigLonnstilskuddBeregningStrategy(avtaleInnhold.getAvtale()));
    }

    @Override
    public Map<String, Object> alleFelterSomMåFyllesUt() {
        Map<String, Object> felterSomMåFyllesUt = super.alleFelterSomMåFyllesUt();
        felterSomMåFyllesUt.put(AvtaleInnhold.Fields.lonnstilskuddFormaal, avtaleInnhold.getLonnstilskuddFormaal());
        return felterSomMåFyllesUt;
    }

    @Override
    public void endre(EndreAvtale endreAvtale) {
        if (LonnstilskuddFormaal.BEHOLDE_ARBEID.equals(endreAvtale.getLonnstilskuddFormaal())) {
            throw new FeilkodeException(Feilkode.FIREARIG_LONNSTILSKUDD_KAN_IKKE_BRUKES_TIL_A_BEHOLDE_ARBEID);
        }

        Integer lonnstilskuddprosentVedStart = beregningStrategy.getProsentForForstePeriode();
        avtaleInnhold.setLonnstilskuddProsent(lonnstilskuddprosentVedStart);
        avtaleInnhold.setLonnstilskuddFormaal(endreAvtale.getLonnstilskuddFormaal());

        super.endre(endreAvtale);
    }

    @Override
    public void endreStillingsbeskrivelse(EndreStillingsbeskrivelse endreStillingsbeskrivelse) {
        if (LonnstilskuddFormaal.BEHOLDE_ARBEID.equals(endreStillingsbeskrivelse.getLonnstilskuddFormaal())) {
            throw new FeilkodeException(Feilkode.FIREARIG_LONNSTILSKUDD_KAN_IKKE_BRUKES_TIL_A_BEHOLDE_ARBEID);
        }

        super.endreStillingsbeskrivelse(endreStillingsbeskrivelse);
    }
}
