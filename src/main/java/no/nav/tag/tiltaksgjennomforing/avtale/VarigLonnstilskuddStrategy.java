package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;

import java.util.Map;

public class VarigLonnstilskuddStrategy extends LonnstilskuddStrategy {
    @Override
    public Map<String, Object> alleFelterSomMåFyllesUt() {
        Map<String, Object> felterSomMåFyllesUt = super.alleFelterSomMåFyllesUt();
        felterSomMåFyllesUt.put(AvtaleInnhold.Fields.lonnstilskuddProsent, avtaleInnhold.getLonnstilskuddProsent());
        return felterSomMåFyllesUt;
    }

    public VarigLonnstilskuddStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold);
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

}
