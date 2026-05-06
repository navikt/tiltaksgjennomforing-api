package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.VarigLonnstilskuddAvtaleBeregningStrategy;

import java.util.Map;

import static no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.VarigLonnstilskuddAvtaleBeregningStrategy.TILSKUDDSPROSENT_MAKS;

public class VarigLonnstilskuddAvtaleInnholdStrategy extends LonnstilskuddAvtaleInnholdStrategy<VarigLonnstilskuddAvtaleBeregningStrategy> {
    public VarigLonnstilskuddAvtaleInnholdStrategy(AvtaleInnhold avtaleInnhold) {
        super(avtaleInnhold, new VarigLonnstilskuddAvtaleBeregningStrategy());
    }

    @Override
    public Map<String, Object> alleFelterSomMåFyllesUt() {
        Map<String, Object> felterSomMåFyllesUt = super.alleFelterSomMåFyllesUt();
        felterSomMåFyllesUt.put(AvtaleInnhold.Fields.lonnstilskuddProsent, avtaleInnhold.getLonnstilskuddProsent());
        return felterSomMåFyllesUt;
    }

    @Override
    public void endre(EndreAvtale endreAvtale) {
        Integer lonnstilskuddProsent = endreAvtale.getLonnstilskuddProsent();

        if (!erTilskuddsprosentGyldig(lonnstilskuddProsent)) {
            throw new FeilLonnstilskuddsprosentException();
        }

        avtaleInnhold.setLonnstilskuddProsent(lonnstilskuddProsent);
        super.endre(endreAvtale);
    }

    private boolean erTilskuddsprosentGyldig(Integer prosent) {
        if (prosent == null) {
            return true;
        }
        return prosent >= 0 && prosent <= TILSKUDDSPROSENT_MAKS;
    }

}
